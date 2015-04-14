# lein-plz

A Leiningen plugin for quickly adding dependencies to projects.

```sh
# Adding org.clojure/core.async, org.clojure/clojurescript
# and org.clojure/data.json to a Leiningen project.

$ lein plz add core.async cljs data.json
```

## Installation
Add `[lein-plz "0.4.0-SNAPSHOT"]` to your user profile, as you
would with other Leiningen plugins.

```clojure
;; ~/.lein/profiles.clj without lein-ancient 

{:user {:plugins [[lein-plz "0.4.0-SNAPSHOT"]]}}
```

However, if you are using lein-ancient, use this instead:

```clojure
;; ~/.lein/profiles.clj with lein-ancient

{:user {:plugins [[lein-ancient "0.6.5"]
                  [lein-plz "0.4.0-SNAPSHOT" :exclusions [[rewrite-clj] [ancient-clj]]]]}}
```

## Search

`lein-plz` uses crossclj to fuzzily discover the latest version of the
dependency you're attempting to add. This means that:

```sh
$ lein plz add data
```

```sh
$ lein plz add data.json
```

```sh
$ lein plz add org.clojure/data.json
```

are all probably equivalent, although times do change!

## Overriding Search

Many major projects have well-known nicknames -- for example,
Clojurescript is usually abbreviated as cljs.

You may specify your own nicknames for projects, and they will take
precedence over search. `lein-plz` ships with a collection of
reasonable nicknames. To view them, write:

```sh
$ lein plz list
```

the list sub-command takes an optional pattern to filter dependencies.

```sh
$ lein plz list org.clojure/
```

It may be the case that you want to add your own nicknames for
dependencies. As an example, lets say these are dependencies you use
most often when you are building websites. Create the file
`~/.plz/server.edn`:

```clojure
{http-kit                        #{"hkit" "hk"}
 compojure                       #{"cjure" "cpj"}}
```

and modify your user profile to look something like the following:

```clojure
{:user {:plugins [[lein-plz "0.4.0-SNAPSHOT"]]}
 :plz  [["/home/edsnowden/.plz/server.edn" :as "server-group"]]}
```

`hk` and `hkit` will now be resolved as `http-kit`, and `cjure` and
`cpj` will be resolved as `compojure`.

Furthermore, you can also write:

```sh
$ lein plz add server-group
```

to add both `http-kit` and `compojure` at once.

## License
Copyright © 2014 John Walker, [@luxbock (Olli Piepponen)](https://github.com/luxbock), @mkremins

Distributed under the Eclipse Public License version 1.0.
