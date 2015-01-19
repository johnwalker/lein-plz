- [lein-plz](#lein-plz)
  - [Usage](#usage)
    - [Nicknames Example](#nicknames-example)
    - [Groups Example](#groups-example)
  - [Setup](#setup)
    - [Adding your own nicknames](#adding-your-own-nicknames)
    - [Adding your own groups](#adding-your-own-groups)
  - [Built-in nicknames](#built-in-nicknames)
  - [Possible Issues](#possible-issues)
    - [Use with lein-ancient](#use-with-lein-ancient)
  - [License](#license)

# lein-plz<a id="sec-1" name="sec-1"></a>

```
ScaRy, ScaRy GUy, BOinG!
GRaPEfRUit fallS!
ScaRy, Sick, BaRfy…
GO anD… la la la!
DinG! ScaRy!

-- Mr. Saturn
```

A Leiningen plugin for quickly adding dependencies to projects.

## Usage<a id="sec-1-1" name="sec-1-1"></a>

### Nicknames Example<a id="sec-1-1-1" name="sec-1-1-1"></a>

Suppose you want to add `clojurescript`, `core.async` and
`data.json` to a project. Maybe you know the latest versions
off-hand (or at least, some version). You still have to type out
the groupIds and &#x2013; it takes some typing.

It's possible to do this instead:

```sh
$ lein plz add core.async cljs data.json
```

### Groups Example<a id="sec-1-1-2" name="sec-1-1-2"></a>

Suppose you're about to build a webapp to advertise
`core.logic`. You've setup groups, so you only need to combine the
dependencies from the server and client.

```sh
$ lein plz add server-group client-group core.logic
```

The result:

```clojure
:dependencies [[org.clojure/clojure "1.6.0"]
               [http-kit "2.1.18"]
               [compojure "1.1.8"]
               [om "0.7.1"]
               [org.clojure/core.async "0.1.338.0-5c5012-alpha"]
               [org.clojure/clojurescript "0.0-2311"]
               [org.clojure/core.logic "0.8.8"]]
```

## Setup<a id="sec-1-2" name="sec-1-2"></a>

Add `[lein-plz "0.3.1"]` to the `:plugins` vector in your user
profile.

```clojure
{:user {:plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]
                  [lein-plz "0.3.1"]
                  [slamhound "1.5.5"]]}}
```

### Adding your own nicknames<a id="sec-1-2-1" name="sec-1-2-1"></a>

`lein-plz` comes prepared with a fairly comprehensive list of fallback nicknames,
which you can see here, or by using the `lein plz list` command. You can filter
the list by adding an additional argument to the command, which is interpreted
as a regexp to display only the matching items:

```sh
lein plz list org.clojure/
```

=>

```
+----------------------------+----------------+
| Dependency                 | Nickname(s)    |
+----------------------------+----------------+
| org.clojure/core.async     | core.async     |
| org.clojure/core.contracts | core.contracts |
| org.clojure/core.logic     | core.logic     |
| org.clojure/core.typed     | core.typed     |
| org.clojure/core.unify     | core.unify     |
| org.clojure/core.match     | core.match     |
| org.clojure/core.cache     | core.cache     |
| org.clojure/core.memoize   | core.memoize   |
+----------------------------+----------------+
```

The list comes from the [CrossClj.info](http://crossclj.info/) web site, where each library with a
popularity score of three and higher is included.

If your favorite library is not present, or you'd like to use a more convenient
nickname to you, then fear not! You can add more nicknames by including your
custom dependency -> nickname map like this (Note: these are built-in nicknames,
so there's no need to include these in your own map):

```clojure
{org.clojure/clojure         #{"clojure" "clj"}
 org.clojure/clojurescript   #{"clojurescript" "cljs"}
 org.clojure/core.async      #{"core.async"}
 compojure                   #{"compojure"}
 hiccup                      #{"hiccup"}
 ring                        #{"ring"}}
```

where the symbols are the full dependency names, while each value is a set of
nicknames for that dependency.

`lein-plz` maintains a global map of dependencies, equivalent to:

```clojure
(merge fallback-nickname-map user-map-1 user-map-2 user-map-3 ...)
```

If the example map resides in `/home/stuartsierra/.plz/myplzmap.edn`, then
`/home/stuartsierra/.lein/profiles.clj` should be:

```clojure
{:user {:plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]
                  [lein-plz "0.3.1"]
                  [slamhound "1.5.5"]]
        :plz ["/home/stuartsierra/.plz/myplzmap.edn"]}}
```

You can use more than one map:

```clojure
{:user {:plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]
                  [lein-plz "0.3.1"]
                  [slamhound "1.5.5"]]
        :plz ["/home/stuartsierra/.plz/myplzmap.edn"
              "/home/stuartsierra/.plz/user-map-2.edn"
              "/home/stuartsierra/.plz/user-map-3.edn"]}}
```

In case of conflicts, the last map always overrides the previous ones, as you'd expect from a `merge`.

### Adding your own groups<a id="sec-1-2-2" name="sec-1-2-2"></a>

You can add collections of dependencies at a time using
groups. Create files containing edn maps such as these:

```clojure
;; server-group
;; /home/stuartsierra/.plz/server.edn
{http-kit                        #{"http-kit"}
 compojure                       #{"compojure"}}

;; client-group
;; /home/stuartsierra/.plz/client.edn
{om                              #{"om"}
 org.clojure/core.async          #{"core.async" "async"}
 org.clojure/clojurescript       #{"clojurescript" "cljs"}}
```

The dependencies in each map can be referenced by following their
filename with :as key and the group's name.

```clojure
{:user {:plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]
                  [lein-plz "0.3.1"]
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

## Built-in nicknames<a id="sec-1-3" name="sec-1-3"></a>

These nicknames are built-in. User options take precedence over
these. View them using `lein plz list`.

### Use with lein-ancient<a id="sec-1-4-1" name="sec-1-4-1"></a>

`lein-plz` uses the same libraries as [lein-ancient](https://github.com/xsc/lein-ancient), the plugin for
upgrading dependencies. It's recommended that users of both
specify the `lein-plz` dependency as follows:

```clojure
[lein-plz "0.3.1" :exclusions [[rewrite-clj] [ancient-clj]]]
```

This is guaranteed to work with lein-ancient version `0.5.9`.

## License<a id="sec-1-5" name="sec-1-5"></a>

Copyright © 2014 John Walker, [@luxbock (Olli Piepponen)](https://github.com/luxbock) 

Distributed under the Eclipse Public License version 1.0.
