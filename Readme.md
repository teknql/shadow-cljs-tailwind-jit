# Shadow-Cljs Tailwind JIT

Build hooks for enabling [Tailwind
JIT](https://tailwindcss.com/docs/just-in-time-mode) within Shadow Projects.

## Installation and configuration

Install the required node dependencies in your project:

```
npm install --save-dev postcss-cli tailwindcss autoprefixer cssnano
```

Add the clojure library to your project via your preferred method (either
shadow's own `deps` or in your `deps.edn` file).

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

## How it works

This library works by sourcing options in your `shadow-cljs.edn` to make a temporary project
project directory with the required files to configure PostCSS + Tailwind to compile CSS for
your project. We then shell out to `postcss-cli` using the temporary configs.
