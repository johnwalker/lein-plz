# lein-plz

```
ScaRy, ScaRy GUy, BOinG!
GRaPEfRUit fallS!
ScaRy, Sick, BaRfy…
GO anD… la la la!
DinG! ScaRy!

-- Mr. Saturn
```

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
### Adding your own nicknames

`lein-plz` comes prepared with a fairly comprehensive list of fallback nicknames,
which you can see by using the `lein plz list` command. You can filter
the list by using a regular expression at the end.

```sh
lein plz list org.clojure/
```

The list comes from the [CrossClj.info](http://crossclj.info/) web site, where each library with a
popularity score of three and higher is included.

You can add more nicknames by storing your custom dependency -> nickname map 
as an edn file like `/home/stuartsierra/.plz/myplzmap.edn`:

```clojure
{org.clojure/clojure         #{"clojure" "clj"}
 org.clojure/clojurescript   #{"clojurescript" "cljs"}
 org.clojure/core.async      #{"core.async"}
 compojure                   #{"compojure"}
 hiccup                      #{"hiccup"}
 ring                        #{"ring"}}
```

each symbol is a dependency name, and the values are sets of nicknames
for that dependency. Enable these nicknames by adding the absolute
path to your `.lein/profiles.clj` and you are done:

```clojure
{:user {:plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]
                  [lein-plz "0.3.3"]
                  [slamhound "1.5.5"]]
        :plz ["/home/stuartsierra/.plz/myplzmap.edn"]}}
```

In case of conflicts, the last map always overrides the previous ones, as you'd expect from a `merge`.

### Adding your own groups

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

The dependencies in each map can be referenced by following their
filename with :as key and the group's name.

```clojure
{:user {:plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]
                  [lein-plz "0.3.3"]
                  [slamhound "1.5.5"]]
        :plz [["/home/stuartsierra/.plz/server.edn" :as "server-group"]
              ["/home/stuartsierra/.plz/client.edn" :as "client-group"]
              ["/home/stuartsierra/.plz/myplzmap.edn"]]}}
```

```sh
$ lein plz add server-group client-group
```

The merge order in adding your own nicknames (See section ) is maintained. [The
wiki has a collection of groups for getting started](https://github.com/johnwalker/lein-plz/wiki/Groups). Feel free to
contribute your own groups to the wiki!

### Use with lein-ancient

`lein-plz` uses the same libraries as [lein-ancient](https://github.com/xsc/lein-ancient), the plugin for
upgrading dependencies. It's recommended that users of both
specify the `lein-plz` dependency as follows:

```clojure
[lein-plz "0.3.3" :exclusions [[rewrite-clj] [ancient-clj]]]
```

This is guaranteed to work with lein-ancient version `0.5.9`.

## License

Copyright © 2014 John Walker, [@luxbock (Olli Piepponen)](https://github.com/luxbock) 

Distributed under the Eclipse Public License version 1.0.
