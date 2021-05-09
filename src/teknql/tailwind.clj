(ns teknql.tailwind
  (:require [jsonista.core :as j]
            [cuerdas.core :as str]
            [babashka.process :as proc])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))

(def default-config
  "Default tailwind config"
  {:future   {}
   :purge    []
   :mode     "jit"
   :theme    {:extend {}}
   :variants {}
   :plugins  []})

(defn- ->json
  "Encode the provided value to JSON"
  [val]
  (j/write-value-as-string
    val
    (j/object-mapper {:encode-key-fn (comp str/camel name)})))

(defn ->export-json
  "Return the provided val as an string with a `module.exports`.

  Used for generating the various *.config.js files that the Node ecosystem loves"
  [val]
  (str "module.exports = " (->json val) ";"))

(defn- cfg-get
  "Behaves identical to `get` but logs the default value back to the user."
  [config key default]
  (or (get config key)
      (do (println "No build config value for " key ". Using default value.")
          default)))

(defn create-tmp-tailwind-project!
  "Create a temporary tailwind project with the necessary assets to build the project using the JIT.

  Return the path to the temporary directory."
  [postcss-cfg tailwind-cfg]
  (let [tmp-dir              (-> (Files/createTempDirectory "tailwind" (make-array FileAttribute 0))
                                 (.toFile)
                                 (.getAbsolutePath))
        tmp-css-path         (str tmp-dir "/" "tailwind.css")
        tmp-tw-cfg-path      (str tmp-dir "/" "tailwind.config.js")
        tmp-postcss-cfg-path (str tmp-dir "/" "postcss.config.js")]
    (spit tmp-css-path "@tailwind base;\n@tailwind components;\n@tailwind utilities;")
    (spit tmp-tw-cfg-path (->export-json tailwind-cfg))
    (spit tmp-postcss-cfg-path (-> postcss-cfg
                                   (assoc-in [:plugins :tailwindcss :config] tmp-tw-cfg-path)
                                   (->export-json)))
    tmp-dir))

(defn start-watch!
  "Start the tailwind JIT"
  {:shadow.build/stage :configure}
  [build-state]
  (let [config      (:shadow.build/config build-state)
        output-path (cfg-get config :tailwind/output "resources/public/css/site.css")
        http-root   (-> config
                        :devtools
                        :http-root)
        tmp-dir     (create-tmp-tailwind-project!
                      {:plugins {:tailwindcss {}}}
                      (merge default-config
                             {:purge [(str http-root "/**/*.js")
                                      (str http-root "/**/*.html")]}
                             (cfg-get config :tailwind/config nil)))]
    (proc/process
      ["./node_modules/.bin/postcss"
       (str tmp-dir "/tailwind.css")
       "--config"
       tmp-dir
       "--watch"
       "-o"
       output-path]
      {:extra-env {"NODE_ENV"      "development"
                   "TAILWIND_MODE" "watch"}
       :err :inherit
       :out :inheirt})
    build-state))

(defn compile-release!
  "Compile the release build of the CSS generated by tailwind."
  {:shadow.build/stage :flush}
  [build-state]
  (let [config      (:shadow.build/config build-state)
        output-path (cfg-get config :tailwind/output "resources/public/css/site.css")
        http-root   (-> config
                        :devtools
                        :http-root)
        tmp-dir     (create-tmp-tailwind-project!
                      {:plugins {:tailwindcss  {}
                                 :autoprefixer {}
                                 :cssnano      {:preset "default"}}}
                      (merge default-config
                             {:purge [(str http-root "/**/*.js")
                                      (str http-root "/**/*.html")]}
                             (cfg-get config :tailwind/config nil)))]
    (-> (proc/process
          ["./node_modules/.bin/postcss"
           (str tmp-dir "/tailwind.css")
           "--config"
           tmp-dir
           "-o"
           output-path]
          {:extra-env {"NODE_ENV"      "production"
                       "TAILWIND_MODE" "build"}})
        deref)
    build-state))
