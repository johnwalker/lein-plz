(ns leiningen.plz
  (:require [ancient-clj.core :as anc]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [leiningen.core.main :as main]
            [rewrite-clj.zip :as z]
            [rewrite-clj.zip.indent :refer [indent]]
            [table.core :refer [table]]))

(def fallback-nicknames
  '{org.clojure/algo.generic                     #{"algo.generic"}
    org.clojure/algo.monads                      #{"algo.monads"}
    org.clojure/clojure                          #{"clojure" "clj"}
    org.clojure/clojurescript                    #{"clojurescript" "cljs"}
    org.clojure/core.async                       #{"core.async"}
    org.clojure/core.cache                       #{"core.cache"}
    org.clojure/core.contracts                   #{"core.contracts"}
    org.clojure/core.logic                       #{"core.logic"}
    org.clojure/core.match                       #{"core.match"}
    org.clojure/core.memoize                     #{"core.memoize"}
    org.clojure/core.typed                       #{"core.typed"}
    org.clojure/core.unify                       #{"core.unify"}
    org.clojure/data.codec                       #{"data.codec"}
    org.clojure/data.csv                         #{"data.csv"}
    org.clojure/data.finger-tree                 #{"data.finger-tree"}
    org.clojure/data.fressian                    #{"data.fressian" "fressian"}
    org.clojure/data.generators                  #{"data.generators"}
    org.clojure/data.json                        #{"data.json"}
    org.clojure/data.priority-map                #{"data.priority-map" "priority-map"}
    org.clojure/data.xml                         #{"data.xml"}
    org.clojure/data.zip                         #{"data.zip"}
    org.clojure/java.classpath                   #{"java.classpath"}
    org.clojure/java.data                        #{"java.data"}
    org.clojure/java.jdbc                        #{"java.jdbc"}
    org.clojure/java.jmx                         #{"java.jmx"}
    org.clojure/jvm.tools.analyzer               #{"jvm.tools.analyzer"}
    org.clojure/math.combinatorics               #{"math.combinatorics" "combinatorics"}
    org.clojure/math.numeric-tower               #{"math.numeric-tower" "numeric-tower"}
    org.clojure/test.check                       #{"test.check"}
    org.clojure/test.generative                  #{"test.generative"}
    org.clojure/tools.analyzer                   #{"tools.analyzer"}
    org.clojure/tools.analyzer.jvm               #{"tools.analyzer.jvm" "t.a.jvm"}
    org.clojure/tools.cli                        #{"tools.cli"}
    org.clojure/tools.macro                      #{"tools.macro"}
    org.clojure/tools.nrepl                      #{"tools.nrepl"}
    org.clojure/tools.reader                     #{"tools.reader"}
    org.clojure/tools.trace                      #{"tools.trace"}

    compojure                                    #{"compojure"}
    hiccup                                       #{"hiccup"}
    ring                                         #{"ring"}

    aleph                                        #{"aleph"}
    amazonica                                    #{"amazonica"}
    analyze                                      #{"analyze"}
    ancient-clj                                  #{"ancient-clj"}
    arrows-extra                                 #{"arrows-extra"}
    auto-reload                                  #{"auto-reload"}
    autodoc                                      #{"autodoc"}
    backtick                                     #{"backtick"}
    base64-clj                                   #{"base64-clj"}
    bond                                         #{"bond"}
    bultitude                                    #{"bultitude"}
    bux                                          #{"bux"}
    byte-streams                                 #{"byte-streams"}
    byte-transforms                              #{"byte-transforms"}
    camel-snake-kebab                            #{"camel-snake-kebab"}
    cascalog                                     #{"cascalog"}
    cheshire                                     #{"cheshire"}
    clansi                                       #{"clansi"}
    classlojure                                  #{"classlojure"}
    clatrix                                      #{"clatrix"}
    clauth                                       #{"clauth"}
    clip-test                                    #{"clip-test"}
    clj-assorted-utils                           #{"clj-assorted-utils"}
    clj-aws-s3                                   #{"clj-aws-s3"}
    clj-campfire                                 #{"clj-campfire"}
    clj-dbcp                                     #{"clj-dbcp"}
    clj-debug                                    #{"clj-debug"}
    clj-glob                                     #{"clj-glob"}
    clj-gui                                      #{"clj-gui"}
    clj-http                                     #{"clj-http"}
    clj-http-fake                                #{"clj-http-fake"}
    clj-http-lite                                #{"clj-http-lite"}
    clj-info                                     #{"clj-info"}
    clj-jgit                                     #{"clj-jgit"}
    clj-json                                     #{"clj-json"}
    clj-kafka                                    #{"clj-kafka"}
    clj-logging-config                           #{"clj-logging-config"}
    clj-native                                   #{"clj-native"}
    clj-oauth                                    #{"clj-oauth"}
    clj-obt                                      #{"clj-obt"}
    clj-pail                                     #{"clj-pail"}
    clj-pail-tap                                 #{"clj-pail-tap"}
    clj-pdf                                      #{"clj-pdf"}
    clj-redis                                    #{"clj-redis"}
    clj-ssh                                      #{"clj-ssh"}
    clj-stacktrace                               #{"clj-stacktrace"}
    clj-statsd                                   #{"clj-statsd"}
    clj-text-decoration                          #{"clj-text-decoration"}
    clj-time                                     #{"clj-time"}
    clj-toml                                     #{"clj-toml"}
    clj-tuple                                    #{"clj-tuple"}
    clj-unit                                     #{"clj-unit"}
    clj-wallhack                                 #{"clj-wallhack"}
    cljs-ajax                                    #{"cljs-ajax"}
    cljs-http                                    #{"cljs-http"}
    cljs-uuid                                    #{"cljs-uuid"}
    cljsbuild                                    #{"cljsbuild"}
    clojail                                      #{"clojail"}
    clojure-complete                             #{"clojure-complete"}
    clojure-csv                                  #{"clojure-csv"}
    clojure-opennlp                              #{"clojure-opennlp"}
    clojure-source                               #{"clojure-source"}
    clojure-tools                                #{"clojure-tools"}
    clojure.options                              #{"clojure.options"}
    clout                                        #{"clout"}
    clucy                                        #{"clucy"}
    codox                                        #{"codox"}
    codox-md                                     #{"codox-md"}
    collection-check                             #{"collection-check"}
    colorize                                     #{"colorize"}
    compliment                                   #{"compliment"}
    conch                                        #{"conch"}
    congomongo                                   #{"congomongo"}
    control                                      #{"control"}
    conveyor                                     #{"conveyor"}
    crate                                        #{"crate"}
    criterium                                    #{"criterium"}
    crypto-equality                              #{"crypto-equality"}
    crypto-random                                #{"crypto-random"}
    datomic-schematode                           #{"datomic-schematode"}
    debug                                        #{"debug"}
    degel-clojure-utils                          #{"degel-clojure-utils"}
    dieter                                       #{"dieter"}
    dire                                         #{"dire"}
    domina                                       #{"domina"}
    dorothy                                      #{"dorothy"}
    easyconf                                     #{"easyconf"}
    ego                                          #{"ego"}
    enfocus                                      #{"enfocus"}
    enlive                                       #{"enlive"}
    environ                                      #{"environ"}
    expectations                                 #{"expectations"}
    fast-zip                                     #{"fast-zip"}
    fetch                                        #{"fetch"}
    figwheel                                     #{"figwheel"}
    filevents                                    #{"filevents"}
    fipp                                         #{"fipp"}
    fobos_clj                                    #{"fobos_clj"}
    fresh                                        #{"fresh"}
    fs                                           #{"fs"}
    fun-utils                                    #{"fun-utils"}
    garden                                       #{"garden"}
    gavagai                                      #{"gavagai"}
    geo-clj                                      #{"geo-clj"}
    geocoder-clj                                 #{"geocoder-clj"}
    gloss                                        #{"gloss"}
    gorilla-renderable                           #{"gorilla-renderable"}
    gui-diff                                     #{"gui-diff"}
    hara                                         #{"hara"}
    hdfs-clj                                     #{"hdfs-clj"}
    hiccups                                      #{"hiccups"}
    hickory                                      #{"hickory"}
    honeysql                                     #{"honeysql"}
    http-kit                                     #{"http-kit"}
    http-kit.fake                                #{"http-kit.fake"}
    http.async.client                            #{"http.async.client"}
    incise-base-hiccup-layouts                   #{"incise-base-hiccup-layouts"}
    incise-core                                  #{"incise-core"}
    incise-git-deployer                          #{"incise-git-deployer"}
    incise-markdown-parser                       #{"incise-markdown-parser"}
    incise-stefon                                #{"incise-stefon"}
    inflections                                  #{"inflections"}
    instaparse                                   #{"instaparse"}
    interval-metrics                             #{"interval-metrics"}
    io                                           #{"io"}
    irclj                                        #{"irclj"}
    jansi-clj                                    #{"jansi-clj"}
    jar-migrations                               #{"jar-migrations"}
    jayq                                         #{"jayq"}
    joplin.core                                  #{"joplin.core"}
    judgr                                        #{"judgr"}
    karras                                       #{"karras"}
    khroma                                       #{"khroma"}
    korma                                        #{"korma"}
    lamina                                       #{"lamina"}
    lancet                                       #{"lancet"}
    lazytest                                     #{"lazytest"}
    leiningen                                    #{"leiningen"}
    leiningen-core                               #{"leiningen-core"}
    leinjacker                                   #{"leinjacker"}
    less-awful-ssl                               #{"less-awful-ssl"}
    lexington                                    #{"lexington"}
    lib-noir                                     #{"lib-noir"}
    liberator                                    #{"liberator"}
    link                                         #{"link"}
    manifold                                     #{"manifold"}
    marginalia                                   #{"marginalia"}
    marshmacros                                  #{"marshmacros"}
    medley                                       #{"medley"}
    midje                                        #{"midje"}
    midje-readme                                 #{"midje-readme"}
    misaki                                       #{"misaki"}
    missing-utils                                #{"missing-utils"}
    native-deps                                  #{"native-deps"}
    necessary-evil                               #{"necessary-evil"}
    noencore                                     #{"noencore"}
    noir                                         #{"noir"}
    ns-tracker                                   #{"ns-tracker"}
    om                                           #{"om"}
    onelog                                       #{"onelog"}
    optimus                                      #{"optimus"}
    ordered                                      #{"ordered"}
    ordered-collections                          #{"ordered-collections"}
    oss-jdbc                                     #{"oss-jdbc"}
    overtone                                     #{"overtone"}
    pail-cascalog                                #{"pail-cascalog"}
    pallet-fsm                                   #{"pallet-fsm"}
    pallet-map-merge                             #{"pallet-map-merge"}
    pallet-thread                                #{"pallet-thread"}
    pathetic                                     #{"pathetic"}
    pedantic                                     #{"pedantic"}
    perforate                                    #{"perforate"}
    peridot                                      #{"peridot"}
    polaris                                      #{"polaris"}
    potemkin                                     #{"potemkin"}
    primitive-math                               #{"primitive-math"}
    proteus                                      #{"proteus"}
    prxml                                        #{"prxml"}
    quickie                                      #{"quickie"}
    quil                                         #{"quil"}
    quoin                                        #{"quoin"}
    radagast                                     #{"radagast"}
    ragtime                                      #{"ragtime"}
    re-rand                                      #{"re-rand"}
    reagent                                      #{"reagent"}
    reply                                        #{"reply"}
    retro                                        #{"retro"}
    rewrite-clj                                  #{"rewrite-clj"}
    rhizome                                      #{"rhizome"}
    riddley                                      #{"riddley"}
    riemann                                      #{"riemann"}
    riemann-clojure-client                       #{"riemann-clojure-client"}
    ring-anti-forgery                            #{"ring-anti-forgery"}
    ring-cors                                    #{"ring-cors"}
    ring-middleware-format                       #{"ring-middleware-format"}
    ring-mock                                    #{"ring-mock"}
    ring-refresh                                 #{"ring-refresh"}
    ring-reload-modified                         #{"ring-reload-modified"}
    ring-serve                                   #{"ring-serve"}
    ring-server                                  #{"ring-server"}
    sablono                                      #{"sablono"}
    sass                                         #{"sass"}
    schema-contrib                               #{"schema-contrib"}
    schematic                                    #{"schematic"}
    scout                                        #{"scout"}
    scriptjure                                   #{"scriptjure"}
    secretary                                    #{"secretary"}
    seesaw                                       #{"seesaw"}
    shaky                                        #{"shaky"}
    slamhound                                    #{"slamhound"}
    sligeom                                      #{"sligeom"}
    slimath                                      #{"slimath"}
    slingshot                                    #{"slingshot"}
    speclj                                       #{"speclj"}
    spyscope                                     #{"spyscope"}
    squarepeg                                    #{"squarepeg"}
    stencil                                      #{"stencil"}
    storm                                        #{"storm"}
    substantiation                               #{"substantiation"}
    swank-clojure                                #{"swank-clojure"}
    swiss-arrows                                 #{"swiss-arrows"}
    table                                        #{"table"}
    tentacles                                    #{"tentacles"}
    test-with-files                              #{"test-with-files"}
    test2junit                                   #{"test2junit"}
    tokyocabinet                                 #{"tokyocabinet"}
    torpo                                        #{"torpo"}
    trammel                                      #{"trammel"}
    transduce                                    #{"transduce"}
    tron                                         #{"tron"}
    valip                                        #{"valip"}
    version-clj                                  #{"version-clj"}
    watchtower                                   #{"watchtower"}
    weasel                                       #{"weasel"}
    wrap-js                                      #{"wrap-js"}
    xrepl                                        #{"xrepl"}
    zookeeper-clj                                #{"zookeeper-clj"}
    zweikopf                                     #{"zweikopf"}
    alandipert/interpol8                         #{"interpol8"}
    am.ik/clj-gae-testing                        #{"clj-gae-testing"}
    aysylu/loom                                  #{"loom"}
    caribou/antlers                              #{"antlers"}
    caribou/caribou-core                         #{"caribou-core"}
    caribou/caribou-frontend                     #{"caribou-frontend"}
    caribou/caribou-plugin                       #{"caribou-plugin"}
    cc.qbits/hayt                                #{"hayt"}
    cider/cider-nrepl                            #{"cider-nrepl"}
    clj-simple-form/clj-simple-form-core         #{"clj-simple-form-core"}
    clojurewerkz/cassaforte                      #{"cassaforte"}
    clojurewerkz/neocons                         #{"neocons"}
    clojurewerkz/ogre                            #{"ogre"}
    clojurewerkz/quartzite                       #{"quartzite"}
    clojurewerkz/support                         #{"support"}
    clojurewerkz/urly                            #{"urly"}
    co.paralleluniverse/pulsar                   #{"pulsar"}
    codox/codox.core                             #{"codox.core"}
    codox/codox.leiningen                        #{"codox.leiningen"}
    com.xab/sanity                               #{"sanity"}
    com.andrewmcveigh/plugin-jquery              #{"plugin-jquery"}
    com.backtype/dfs-datastores                  #{"dfs-datastores"}
    com.birdseye-sw/dalap                        #{"dalap"}
    com.birdseye-sw/lein-dalap                   #{"lein-dalap"}
    com.cemerick/austin                          #{"austin"}
    com.cemerick/clojurescript.test              #{"clojurescript.test"}
    com.cemerick/double-check                    #{"double-check"}
    com.cemerick/friend                          #{"friend"}
    com.cemerick/piggieback                      #{"piggieback"}
    com.cemerick/pomegranate                     #{"pomegranate"}
    com.cemerick/pprng                           #{"pprng"}
    com.damballa/abracad                         #{"abracad"}
    com.flyingmachine/webutils                   #{"webutils"}
    com.jakemccrary/lein-test-refresh            #{"lein-test-refresh"}
    com.keminglabs/cljx                          #{"cljx"}
    com.keminglabs/singult                       #{"singult"}
    com.madeye.clojure.ampache/ampachedb         #{"ampachedb"}
    com.madeye.clojure.common/common             #{"common"}
    com.mdrogalis/onyx                           #{"onyx"}
    com.novemberain/langohr                      #{"langohr"}
    com.novemberain/monger                       #{"monger"}
    com.novemberain/pantomime                    #{"pantomime"}
    com.novemberain/validateur                   #{"validateur"}
    com.onekingslane.danger/clojure-common-utils #{"clojure-common-utils"}
    com.palletops/api-builder                    #{"api-builder"}
    com.palletops/discovery-api-runtime          #{"discovery-api-runtime"}
    com.palletops/git-crate                      #{"git-crate"}
    com.palletops/java-crate                     #{"java-crate"}
    com.palletops/pallet-common                  #{"pallet-common"}
    com.palletops/rbenv-crate                    #{"rbenv-crate"}
    com.palletops/upstart-crate                  #{"upstart-crate"}
    com.redhat.qe/jul.test.records               #{"jul.test.records"}
    com.relaynetwork/clorine                     #{"clorine"}
    com.ryanmcg/incise-codox                     #{"incise-codox"}
    com.ryanmcg/incise-vm-layout                 #{"incise-vm-layout"}
    com.stuartsierra/component                   #{"component"}
    com.stuartsierra/dependency                  #{"dependency"}
    com.taoensso/carmine                         #{"carmine"}
    com.taoensso/encore                          #{"encore"}
    com.taoensso/faraday                         #{"faraday"}
    com.taoensso/nippy                           #{"nippy"}
    com.taoensso/sente                           #{"sente"}
    com.taoensso/timbre                          #{"timbre"}
    com.taoensso/tower                           #{"tower"}
    com.velisco/tagged                           #{"tagged"}
    dar/container                                #{"container"}
    dk.ative/docjure                             #{"docjure"}
    factual/drake-interface                      #{"drake-interface"}
    grimradical/clj-semver                       #{"clj-semver"}
    hornetq-clj/server                           #{"server"}
    im.chit/hara.class.inheritance               #{"hara.class.inheritance"}
    im.chit/hara.common.checks                   #{"hara.common.checks"}
    im.chit/hara.common.error                    #{"hara.common.error"}
    im.chit/hara.common.hash                     #{"hara.common.hash"}
    im.chit/hara.common.state                    #{"hara.common.state"}
    im.chit/hara.common.string                   #{"hara.common.string"}
    im.chit/hara.common.watch                    #{"hara.common.watch"}
    im.chit/hara.data.combine                    #{"hara.data.combine"}
    im.chit/hara.data.map                        #{"hara.data.map"}
    im.chit/hara.data.nested                     #{"hara.data.nested"}
    im.chit/hara.expression.form                 #{"hara.expression.form"}
    im.chit/hara.expression.shorthand            #{"hara.expression.shorthand"}
    im.chit/hara.function.args                   #{"hara.function.args"}
    im.chit/hara.function.dispatch               #{"hara.function.dispatch"}
    im.chit/hara.namespace.import                #{"hara.namespace.import"}
    im.chit/hara.namespace.resolve               #{"hara.namespace.resolve"}
    im.chit/hara.protocol.state                  #{"hara.protocol.state"}
    im.chit/hara.sort.topological                #{"hara.sort.topological"}
    im.chit/hara.string.path                     #{"hara.string.path"}
    im.chit/iroh                                 #{"iroh"}
    im.chit/purnam.common                        #{"purnam.common"}
    im.chit/purnam.test                          #{"purnam.test"}
    im.chit/ribol                                #{"ribol"}
    im.chit/vinyasa.inject                       #{"vinyasa.inject"}
    im.chit/vinyasa.lein                         #{"vinyasa.lein"}
    info.hoetzel/clj-nio2                        #{"clj-nio2"}
    io.aviso/pretty                              #{"pretty"}
    io.mandoline/mandoline-core                  #{"mandoline-core"}
    jarohen/chord                                #{"chord"}
    jarohen/nomad                                #{"nomad"}
    jeremys/cljss-core                           #{"cljss-core"}
    jig/protocols                                #{"protocols"}
    jonase/eastwood                              #{"eastwood"}
    jonase/kibit                                 #{"kibit"}
    kixi/data.vendor.parent                      #{"data.vendor.parent"}
    listora/constraint                           #{"constraint"}
    longstorm/claude                             #{"claude"}
    lonocloud/synthread                          #{"synthread"}
    me.raynes/cegdown                            #{"cegdown"}
    meridian/shapes                              #{"shapes"}
    metosin/ring-http-response                   #{"ring-http-response"}
    metosin/ring-swagger                         #{"ring-swagger"}
    metosin/ring-swagger-ui                      #{"ring-swagger-ui"}
    mvxcvi/puget                                 #{"puget"}
    name.rumford/clojure-carp                    #{"clojure-carp"}
    net.cgrand/moustache                         #{"moustache"}
    net.colourcoding/poppea                      #{"poppea"}
    net.drib/mrhyde                              #{"mrhyde"}
    net.intensivesystems/arrows                  #{"arrows"}
    net.mikera/core.matrix                       #{"core.matrix"}
    net.mikera/vectorz-clj                       #{"vectorz-clj"}
    org.blancas/kern                             #{"kern"}
    org.blancas/morph                            #{"morph"}
    org.bodil/cljs-noderepl                      #{"cljs-noderepl"}
    org.bodil/lein-noderepl                      #{"lein-noderepl"}
    org.bodil/redlobster                         #{"redlobster"}
    org.dthume/data.set                          #{"data.set"}
    org.flatland/chronicle                       #{"chronicle"}
    org.flatland/laminate                        #{"laminate"}
    org.flatland/telegraph-js                    #{"telegraph-js"}
    org.flatland/useful                          #{"useful"}
    org.immutant/deploy-tools                    #{"deploy-tools"}
    org.maravillas/ring-core-gae                 #{"ring-core-gae"}
    org.markdownj/markdownj                      #{"markdownj"}
    org.ozias.cljlibs/scm                        #{"scm"}
    org.ozias.cljlibs/shell                      #{"shell"}
    org.scribe/scribe                            #{"scribe"}
    org.thnetos/cd-client                        #{"cd-client"}
    org.tobereplaced/mapply                      #{"mapply"}
    pjstadig/scopes                              #{"scopes"}
    prismatic/dommy                              #{"dommy"}
    prismatic/fnhouse                            #{"fnhouse"}
    prismatic/om-tools                           #{"om-tools"}
    prismatic/plumbing                           #{"plumbing"}
    prismatic/schema                             #{"schema"}
    puppetlabs/http-client                       #{"http-client"}
    puppetlabs/kitchensink                       #{"kitchensink"}
    puppetlabs/trapperkeeper                     #{"trapperkeeper"}
    purnam/purnam-js                             #{"purnam-js"}
    reiddraper/simple-check                      #{"simple-check"}
    ring/ring-codec                              #{"ring-codec"}
    ring/ring-core                               #{"ring-core"}
    ring/ring-devel                              #{"ring-devel"}
    ring/ring-jetty-adapter                      #{"ring-jetty-adapter"}
    ring/ring-json                               #{"ring-json"}
    ring/ring-servlet                            #{"ring-servlet"}
    ritz/ritz-debugger                           #{"ritz-debugger"}
    ritz/ritz-repl-utils                         #{"ritz-repl-utils"}
    robert/bruce                                 #{"bruce"}
    robert/hooke                                 #{"hooke"}
    ruiyun/tools.timer                           #{"tools.timer"}
    shoreleave/shoreleave-browser                #{"shoreleave-browser"}
    shoreleave/shoreleave-core                   #{"shoreleave-core"}
    shoreleave/shoreleave-remote                 #{"shoreleave-remote"}
    shoreleave/shoreleave-remote-ring            #{"shoreleave-remote-ring"}
    sonian/carica                                #{"carica"}
    tailrecursion/cljs-priority-map              #{"cljs-priority-map"}
    tailrecursion/cljson                         #{"cljson"}
    the/parsatron                                #{"parsatron"}
    up/up-core                                   #{"up-core"}})

(defn lookup-nick
  [nickname-map nick]
  (->> nickname-map
       (filter (fn [[dependency set-of-nicks]]
                 (when (set-of-nicks nick)
                   dependency)))
       first first))

(defn to-updated-pair [s]
  [s (anc/latest-version-string! {:snapshots? false} s)])

(defn determine-case [prj-map]
  (if-let [z-deps (z/find-value prj-map :dependencies)]
    (let [z-deps-v (z/right z-deps)
          s-deps-v (z/sexpr z-deps-v)]
      [(if (seq s-deps-v)
         (if (vector? s-deps-v)
           :some-vector
           :some-sequence)
         (if (vector? s-deps-v)
           :empty-vector
           :something-else)) z-deps-v])
    [:no-dep-key prj-map]))

(defn get-deps [[k z]]
  (if (= k :some-vector)
    (set (map first (z/sexpr z)))
    #{}))

(defn insert-dep [z dep]
  (-> z
      (z/insert-right dep)
      (z/append-newline)
      (z/right)
      (indent 16)))

(defn conj-deps
  [[k z] deps]
  (try [true
        (case k
          :some-vector (let [z' (-> z z/down z/rightmost)]
                         (reduce insert-dep
                                 z'
                                 deps))

          :empty-vector (reduce insert-dep
                                (-> z
                                    (z/replace [(first deps)])
                                    (z/down))
                                (rest deps))
          :no-dep-key     (throw (ex-info :no-dep-key {}))
          :some-sequence  (throw (ex-info :some-sequence {}))
          :something-else (throw (ex-info :something-else {})))]
       (catch Exception e
         [false (.getMessage e)])))

(defn warn [m]
  (main/info "Warning:" m))

(defn generate-bug-report [ex]
  (let [m (ex-data ex)]
    (doseq [x [(.getMessage ex)
               "File a bug report here: "
               "https://github.com/johnwalker/lein-plz"
               "--"]]
      (main/info x))
    (main/info m)))

(defn print-instructions []
  (main/info
    (str
      "Add dependencies to your existing project.clj file.\n"
      "Use the following commands:\n\n"
      "  - add <nick or nicks>"
      " -- Adds the dependencies associated with each nick to the project.\n"
      "    Example: lein plz add core.async cljs data.json\n\n"
      "  - list <& filter> -- List the built-in "
      "(total " (count fallback-nicknames) ") dependencies with their nicknames.\n"
      "    If called with a filter, it will be passed as a regular expression "
      "to limit the amount of entries.\n"
      "    Example: lein plz list org.clojure/")))

;;; Listing

(defn match-entry? [s [path nicks]]
  (let [pattern (re-pattern (str s))]
    (or (re-find pattern (str path))
        (some not-empty (map (partial re-find pattern) nicks)))))

(defn search-nicks [s] (filter (partial match-entry? s) fallback-nicknames))

(defn format-entry [[path nicks]] [path (str/join ", " nicks)])

(defn format-entries [matches]
  (->> matches
       (map format-entry)
       (cons ["Dependency" "Nickname(s)"])
       table))

(defn list-nicks [s] (-> s search-nicks format-entries))

(defn list-all [] (format-entries (seq fallback-nicknames)))


;; Adding

(defn entry-parser [entry]
  (cond
    (and (vector? entry)
         (= :as (get entry 1))
         (get entry 2))
    [(get entry 2)
     (-> (first entry)
         slurp
         read-string)]
    (vector? entry)
    [nil
     (-> (first entry)
         slurp
         read-string)]
    (string? entry)
    [nil
     (-> entry
         slurp
         read-string)]))

(defn parse-options [options]
  (cond
    (string? options) [nil (read-string (slurp options))]
    (map? options)    [nil options]
    (seq options)     (reduce
                        (fn [[groups global]
                             [group-name group]]
                          [(if group-name
                             (assoc groups group-name (-> group keys vec))
                             groups)
                           (merge global group)])
                        [nil nil]
                        (mapv entry-parser options))
    :else             (when (seq options)
                        (throw (ex-info "Merge" {:plz-options options})))))

(defn recognize-deps [present-deps m]
  (let [known-pairs (future
                      (->> (get-in m [true])
                           (map second)
                           (remove present-deps)
                           (pmap to-updated-pair)
                           (distinct)))
        unknown-deps (map first (get-in m [false]))]
    (doseq [u unknown-deps]
      (main/info "Unrecognized nickname" u))
    @known-pairs))

(defn nicks->deps [nicks present-deps groups af-map]
  (->> nicks
       (distinct)
       (reduce (fn [normalized-deps word]
                 (if-let [g (groups word)]
                   (into normalized-deps (sort (map (partial vector word) g)))
                   (conj normalized-deps
                     [word (lookup-nick af-map word)]))) [])
       (group-by (comp not nil? second))
       (recognize-deps present-deps)))

(defn add-deps [project nicks]
  (let [root            (:root project)
        project-file    (str root "/" "project.clj")
        project-str     (slurp project-file)
        [groups af-map] (parse-options (:plz project))
        af-map          (merge fallback-nicknames af-map)
        groups          (or groups {})
        prj-map         (-> (z/of-string project-str)
                          (z/find-value z/next 'defproject))
        [k z]           (determine-case prj-map)
        present-deps    (get-deps [k z])
        deps            (nicks->deps nicks present-deps groups af-map)]
    (when-not (seq (:plz project))
      (warn "Using default nicknames since no options were found."))
    (let [[left right] (conj-deps [k z] deps)]
      (if left
        (let [output (with-out-str (z/print-root right))]
          (if (>= (count output) (count project-str))
            (spit project-file output)
            (throw (ex-info "Output shrunk" {:project-str project-str
                                             :attempted-output output}))))
        (throw (ex-info "Something went wrong" {:left left
                                                :right right}))))))

;;; Main

(defn plz
  "Add dependencies using their nicknames."
  [project & stuff]
  (let [[action & args] stuff]
    (try
      (cond
        (and (= "add" action) (seq args)) (add-deps project args)
        (and (= "list" action) (seq args)) (list-nicks (str/join " " args))
        (= "list" action) (list-all)
        (nil? action) (print-instructions)
        :else (do (main/info (format "\"%s\" is not a proper command."))
                  (print-instructions)))
      (catch Exception e
        (generate-bug-report e)))))
