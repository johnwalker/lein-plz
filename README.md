# lein-plz

A Leiningen plugin for quickly adding dependencies to projects.

```sh
$ lein plz add core.async cljs data.json
```
## Setup

Add `[lein-plz "0.3.3"]` to the `:plugins` vector in your user
profile.

```clojure
{:user {:plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]
                  [lein-plz "0.3.3"]
                  [slamhound "1.5.5"]]}}
```
### Adding nicknames

Nicknames are alternative names of projects. They can make project setup
more convenient, since they can be shorter and more memorable. `lein-plz` 
comes with a fairly comprehensive list of nicknames, which you can see by
running `lein plz list`.

```sh
lein plz list org.clojure/
```

You can add more nicknames by storing your custom dependency -> nickname map 
as an edn file such as `/home/stuartsierra/.plz/myplzmap.edn`:

```clojure
{org.clojure/clojure         #{"clojure" "clj"}
 org.clojure/clojurescript   #{"clojurescript" "cljs"}
 org.clojure/core.async      #{"core.async"}
 compojure                   #{"compojure"}
 hiccup                      #{"hiccup"}
 ring                        #{"ring"}}
```

Each symbol is a dependency name, and the values are sets of nicknames
for that dependency. Enable these nicknames by adding the absolute
path to your `.lein/profiles.clj` and you are done:

```clojure
{:user {:plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]
                  [lein-plz "0.3.3"]
                  [slamhound "1.5.5"]]
        :plz ["/home/stuartsierra/.plz/myplzmap.edn"]}}
```

In case of conflicts, the last map always overrides the previous ones, as you'd expect from a `merge`.

### Adding groups

You can add collections of dependencies at a time using
groups. Create files containing edn maps such as these:

```clojure
;; server-group
;; /home/stuartsierra/.plz/server.edn
{http-kit                        #{"http-kit"}
 compojure                       #{"compojure"}}

;; client-group
;; /home/stuartsierra/.plz/client.edn
{org.omcljs/om                   #{"om"}
 org.clojure/core.async          #{"core.async" "async"}
 org.clojure/clojurescript       #{"clojurescript" "cljs"}}
```

The dependencies in each map can be referenced from the command line 
by following their filename with the `:as` keyword and the group's name.

```clojure
{:user {:plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]
                  [lein-plz "0.3.3"]
                  [slamhound "1.5.5"]]
        :plz [["/home/stuartsierra/.plz/server.edn" :as "server-group"]
              ["/home/stuartsierra/.plz/client.edn" :as "client-group"]
              ["/home/stuartsierra/.plz/myplzmap.edn"]]}}
```

Now you can add all dependencies in that map at once.

```sh
$ lein plz add server-group client-group
```

### Use with lein-ancient

`lein-plz` uses the same libraries as [lein-ancient](https://github.com/xsc/lein-ancient), the plugin for
upgrading dependencies. It's recommended that users of both
specify the `lein-plz` dependency as follows:

```clojure
[lein-plz "0.3.3" :exclusions [[rewrite-clj] [ancient-clj]]]
```

`lein-plz` is known to work with lein-ancient version `0.5.9`.

## License

Copyright © 2014 John Walker, [@luxbock (Olli Piepponen)](https://github.com/luxbock) 

Distributed under the Eclipse Public License version 1.0.
