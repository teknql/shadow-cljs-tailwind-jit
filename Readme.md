# Shadow-Cljs Tailwind JIT
[![Clojars Project](https://img.shields.io/clojars/v/com.teknql/shadow-cljs-tailwind-jit.svg)](https://clojars.org/com.teknql/shadow-cljs-tailwind-jit)

Build hooks for enabling [Tailwind
JIT](https://tailwindcss.com/docs/just-in-time-mode) within Shadow Projects.

## Installation and configuration

Install the required node dependencies in your project:

```
npm install --save-dev postcss postcss-cli tailwindcss autoprefixer cssnano
```

Add the clojure library to your project via your preferred method (either
shadow's own `deps` or in your `deps.edn` file).

```clj
{com.teknql/shadow-cljs-tailwind-jit
 {:mvn/version "0.2.2"}}
```

Next, add the required build hooks to your `shadow-cljs.edn` build configuration:


```clj
{:builds
 {:ui
  {;; ...
   :dev
   {:build-hooks
    [(teknql.tailwind/start-watch!)]}}
   :release
   {:build-hooks
    [(teknql.tailwind/compile-release!)]}
   :devtools
   {:http-root   "resources/public/" ;; Must be set to infer default purge targets.
    :http-port   3000}
   :tailwind/output "resources/public/css/site.css"}}}
```

## Customization

The following options are supported via namespaced keys within the `shadow-cljs` build config:


- `:tailwind/output` - Where the generated CSS will be written to. Default:
  `resources/public/css/site.css`
- `:tailwind/config` - A map that is used for the `tailwind.config.js`. Automatically encodes kebab
  cased keys into camel cased.
- `:postcss/config` - A map that is used for the `postcss.config.js`. Automatically encodes kebab
  cased keys into camel cased.

Note that editing either the `:tailwind/config` or `:postcss/config` could result in incompatible
configurations, so please be careful.

### Using with Tailwind Config Files

If your project is sufficiently complex, you may be best off using the
`tailwind.config.js`, `postcss.config.js`, and a `.css` file entrypoint. In this
case you're just using shadow to manage your postcss process. To do this you can
use the `:tailwind/files` variable.

```clj
{:tailwind/files
  {:base-path "./path" ;; Path to directory housing `tailwind.config.js` and `postcss.config.js`
   :tailwind.css "./path/style.css"}} ;; Path to tailwind entrypoint
```

## FAQ

### How is this different from [jacekschae/shadow-cljs-tailwindcss](https://github.com/jacekschae/shadow-cljs-tailwindcss)?

The above project is a great example of how to get tailwindcss and postcss set up to use
along side a shadow-cljs project. When running it's `npm run dev` it will start shadow-cljs as well
as a postcss watch process to continuously recompile your stylesheets. Since it uses tailwind and
postcss directly, it is reliant on your project having some boilerplate (`tailwind.config.js`,
`postcss.config.js` and `src/css/tailwind.css`) that those tools expect to be in place.

This project differs in two major ways:

1) It instead uses shadow-cljs' build-hooks machinery to manage the postcss process. The end result
is the same, but results in you being able to call `npx shadow-cljs server` and have the postcss
process started for you. If you use tools in your editor to manage your shadow process (eg. cider)
then this will require less configuration.

2) Rather than introduce the boilerplate files mentioned above, the build-hooks create a temporary
project with the required boilerplate, allowing you to omit the files from your project.
Configuration can still be provided using the `:tailwind/config` and `:postcss/config` entries
in the build configuration of your project in your `shadow-cljs.edn`
