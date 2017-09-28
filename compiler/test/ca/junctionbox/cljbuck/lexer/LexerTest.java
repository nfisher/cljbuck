package ca.junctionbox.cljbuck.lexer;

import ca.junctionbox.cljbuck.channel.ReadWriterQueue;
import org.junit.Test;

import static ca.junctionbox.cljbuck.lexer.ItemType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class LexerTest {
    public static final String pedestalInterceptorHelpers = "; Copyright 2013 Relevance, Inc.\n" +
            "; Copyright 2014-2016 Cognitect, Inc.\n" +
            "\n" +
            "; The use and distribution terms for this software are covered by the\n" +
            "; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)\n" +
            "; which can be found in the file epl-v10.html at the root of this distribution.\n" +
            ";\n" +
            "; By using this software in any fashion, you are agreeing to be bound by\n" +
            "; the terms of this license.\n" +
            ";\n" +
            "; You must not remove this notice, or any other, from this software.\n" +
            "\n" +
            "(ns io.pedestal.interceptor.helpers\n" +
            "  (:require [io.pedestal.interceptor :as interceptor :refer [interceptor\n" +
            "                                                             interceptor-name]]))\n" +
            "\n" +
            "(defmacro definterceptor\n" +
            "  \"Define an instance of an interceptor and store it in a var. An\n" +
            "  optional doc string can be provided.\n" +
            "  The body can be anything that satisfies the IntoInterceptor protocol.\n" +
            "\n" +
            "  usage:\n" +
            "    (definterceptor encode-response\n" +
            "       \\\"An interceptor that encodes the response as json\\\"\n" +
            "       (on-response encode-json))\n" +
            "\n" +
            "  Alternatively, you may also:\n" +
            "    (def encode-response\n" +
            "      \\\"An interceptor that encodes the response as json\\\"\n" +
            "      (on-response encode-json)\"\n" +
            "  [name & body]\n" +
            "  (let [init (if (string? (first body))\n" +
            "               (second body)\n" +
            "               (first body))\n" +
            "        doc (when (string? (first body))\n" +
            "              (first body))]\n" +
            "    `(def ~(with-meta name {:doc doc})  ~(interceptor init))))\n" +
            "\n" +
            "(defn- infer-basic-interceptor-function\n" +
            "  \"Given list `args`, infer a form that will evaluate to a function\n" +
            "  from the list. The passed list will be interpreted as entirely\n" +
            "  containing information for specifying one function, and no\n" +
            "  additional information.\"\n" +
            "  [args]\n" +
            "  (cond\n" +
            "   (symbol? (first args)) (first args)\n" +
            "   (and (vector? (first args))\n" +
            "        (< 1 (count args))) `(fn ~@args)))\n" +
            "\n" +
            "(defn- infer-first-interceptor-function\n" +
            "  \"Given list `args`, infer a form that will evaluate to a function\n" +
            "  from the list. The passed list will be interpreted as potentially\n" +
            "  containing information for specifying multiple functions, and only\n" +
            "  information for the first function will be inferred from the list.\"\n" +
            "  [args]\n" +
            "  (cond\n" +
            "   (symbol? (first args)) (first args)\n" +
            "   (list? (first args)) (conj (first args) 'fn)))\n" +
            "\n" +
            "(defn infer-rest-interceptor-function\n" +
            "  \"Given list `args`, return the rest of args that would remain after\n" +
            "  removing the elements of args that specify the form returned by\n" +
            "  infer-first-interceptor-function.\"\n" +
            "  [args]\n" +
            "  ;; Right now there's no ambiguity, so just use rest.\n" +
            "  (rest args))\n" +
            "\n" +
            "(defmacro ^{:private true} defsimpleinterceptordef\n" +
            "  \"Defines a macro which is used for defining interceptor defs. `n`\n" +
            "  is the name of an interceptorfn which adapts a fn to the interceptor\n" +
            "  framework, e.g. before, `docstring` is the docstring for the macro in question.\"\n" +
            "  [n docstring]\n" +
            "  `(defmacro ~(symbol (str \"def\" (name n)))\n" +
            "     ~docstring\n" +
            "     [macro-name# & args#]\n" +
            "     (let [[docstring# args#] (if (string? (first args#))\n" +
            "                                [(first args#) (rest args#)]\n" +
            "                                [nil args#])\n" +
            "           prefix-list# (if docstring#\n" +
            "                          (list macro-name# docstring#)\n" +
            "                          (list macro-name#))\n" +
            "           fn-form# (infer-basic-interceptor-function args#)\n" +
            "           interceptor-name# (keyword (name (ns-name *ns*)) (name macro-name#))]\n" +
            "       `(definterceptor ~@prefix-list#\n" +
            "          (~~n ~interceptor-name# ~fn-form#)))))\n" +
            "\n" +
            "(defn before\n" +
            "  \"Returns an interceptor which calls `f` on context during the enter\n" +
            "  stage.\"\n" +
            "  ([f] (interceptor {:enter f}))\n" +
            "  ([f & args]\n" +
            "     (let [[n f args] (if (fn? f)\n" +
            "                        [nil f args]\n" +
            "                        [f (first args) (rest args)])]\n" +
            "       (interceptor {:name (interceptor-name n)\n" +
            "                     :enter #(apply f % args)}))))\n" +
            "\n" +
            "(defsimpleinterceptordef before \"Defines a before interceptor. The\n" +
            "  defined function performs processing during interceptor execution\n" +
            "  during the enter stage. The implicitly created function will operate\n" +
            "  on context, and return a value used as the new context, e.g.:\n" +
            "\n" +
            "  (defbefore flag-zotted\n" +
            "    [context]\n" +
            "    (assoc context :zotted true))\")\n" +
            "\n" +
            "(defn after\n" +
            "  \"Return an interceptor which calls `f` on context during the leave\n" +
            "  stage.\"\n" +
            "  ([f] (interceptor {:leave f}))\n" +
            "  ([f & args]\n" +
            "     (let [[n f args] (if (fn? f)\n" +
            "                        [nil f args]\n" +
            "                        [f (first args) (rest args)])]\n" +
            "       (interceptor {:name (interceptor-name n)\n" +
            "                     :leave #(apply f % args)}))))\n" +
            "\n" +
            "(defsimpleinterceptordef after\n" +
            "  \"Defines an after interceptor. The defined function is processed\n" +
            "  during the leave stage of interceptor execution. The implicitly\n" +
            "  created function will operate on context, and return a value used as\n" +
            "  the new context, e.g.:\n" +
            "\n" +
            "  (defafter check-zotted\n" +
            "    [context]\n" +
            "    (if-not (:zotted context)\n" +
            "      (throw (ex-info \\\"Context was not zotted!\\\"\n" +
            "                      {:context context}))\n" +
            "      context))\")\n" +
            "\n" +
            "(defn around\n" +
            "  \"Return an interceptor which calls `f1` on context during the enter\n" +
            "  stage, and calls `f2` on context during the leave stage.\"\n" +
            "  ([f1 f2]\n" +
            "     (interceptor {:enter f1\n" +
            "                   :leave f2}))\n" +
            "  ([n f1 f2]\n" +
            "     (interceptor {:name (interceptor-name n)\n" +
            "                   :enter f1\n" +
            "                   :leave f2})))\n" +
            "\n" +
            "(defmacro defaround\n" +
            "  \"Defines an around interceptor. The definition resembles a multiple\n" +
            "  arity function definition, however both fns are 1-arity. The first\n" +
            "  fn will be called during the enter stage, the second during the\n" +
            "  leave stage, e.g.:\n" +
            "\n" +
            "  (defaround aroundinterceptor\n" +
            "    ([context] (assoc context :around :entering))\n" +
            "    ([context] (assoc context :around :leaving)))\"\n" +
            "\n" +
            "  [n & args]\n" +
            "  (let [[docstring args] (if (string? (first args))\n" +
            "                           [(first args) (rest args)]\n" +
            "                           [nil args])\n" +
            "        prefix-list (if docstring\n" +
            "                      (list n docstring)\n" +
            "                      (list n))\n" +
            "        [enter-fn-form args] [(infer-first-interceptor-function args)\n" +
            "                              (infer-rest-interceptor-function args)]\n" +
            "        [leave-fn-form args] [(infer-first-interceptor-function args)\n" +
            "                              (infer-rest-interceptor-function args)]\n" +
            "        interceptor-name# (keyword (name (ns-name *ns*)) (name n))]\n" +
            "    `(definterceptor ~@prefix-list\n" +
            "       (around ~interceptor-name# ~enter-fn-form ~leave-fn-form))))\n" +
            "\n" +
            "(defn on-request\n" +
            "  \"Returns an interceptor which updates the :request value of context\n" +
            "  with f during the enter stage.\"\n" +
            "  ([f] (before (fn [context]\n" +
            "                 (assoc context :request (f (:request context))))))\n" +
            "  ([f & args]\n" +
            "     (let [[n f args] (if (fn? f)\n" +
            "                        [nil f args]\n" +
            "                        [f (first args) (rest args)])]\n" +
            "       (interceptor {:name (interceptor-name n)\n" +
            "                     :enter (fn [context]\n" +
            "                              (assoc context :request (apply f (:request context) args)))}))))\n" +
            "\n" +
            "(defsimpleinterceptordef on-request\n" +
            "  \"Defines an on-request interceptor. The definition performs\n" +
            "  pre-processing on a request during the enter stage of interceptor\n" +
            "  execution. The implicitly created interceptor will extract the\n" +
            "  request from the context it receives, pass it to the defined\n" +
            "  function, and then associate the return value from the defined\n" +
            "  function as into context with the :request key and return\n" +
            "  context, e.g.:\n" +
            "\n" +
            "  (defon-request parse-body-as-wibblefish\n" +
            "    [request]\n" +
            "    (assoc request :wibblefish-params\n" +
            "           (wibblefish-parse (:body request))))\n" +
            "\n" +
            "  This is equivalent to:\n" +
            "\n" +
            "  (defbefore parse-body-as-wibblefish\n" +
            "    [context]\n" +
            "    (let [request (:request context)\n" +
            "          new-request (assoc request :wibblefish-params\n" +
            "                             (wibblefish-parse (:body request)))]\n" +
            "      (assoc context :request new-request)))\")\n" +
            "\n" +
            "(defn on-response\n" +
            "  \"Returns an interceptor which updates the :response value of context\n" +
            "  with f during the leave stage.\"\n" +
            "  ([f] (after (fn [context]\n" +
            "                (assoc context :response (f (:response context))))))\n" +
            "  ([f & args]\n" +
            "     (let [[n f args] (if (fn? f)\n" +
            "                        [nil f args]\n" +
            "                        [f (first args) (rest args)])]\n" +
            "       (interceptor {:name (interceptor-name n)\n" +
            "                     :leave (fn [context]\n" +
            "                              (assoc context :response (apply f (:response context) args)))}))))\n" +
            "\n" +
            "(defsimpleinterceptordef on-response\n" +
            "  \"Defines an on-response interceptor. The definition performs post\n" +
            "  processing on a response during the leave stage of interceptor\n" +
            "  execution. The implicitly created interceptor will extract the\n" +
            "  response from the context it receives, pass it to the defined\n" +
            "  function, and then associate the return value from the defined function\n" +
            "  into context with the :response key and return context, e.g.:\n" +
            "\n" +
            "  (defon-response change-body-to-html\n" +
            "    [response]\n" +
            "    (assoc response :body\n" +
            "           (render-to-html (:body response))))\n" +
            "\n" +
            "  This is equivalent to:\n" +
            "\n" +
            "  (defafter change-body-to-html\n" +
            "    [context]\n" +
            "    (let [response (:response context)\n" +
            "          new-response (assoc response :body\n" +
            "                              (render-to-html (:body response)))]\n" +
            "      (assoc context :response new-response)))\")\n" +
            "\n" +
            "(defn handler\n" +
            "  \"Returns an interceptor which calls f on the :request value of\n" +
            "  context, and assoc's the return value as :response into context during the\n" +
            "  enter stage.\"\n" +
            "  ([f]\n" +
            "     (before (fn [context]\n" +
            "               (assoc context :response (f (:request context))))))\n" +
            "  ([n f]\n" +
            "     (before (interceptor-name n)\n" +
            "             (fn [context]\n" +
            "               (assoc context :response (f (:request context)))))))\n" +
            "\n" +
            "(defsimpleinterceptordef handler\n" +
            "  \"Defines a handler interceptor. The definition mirrors a ring-style\n" +
            "  request handler and is made in terms of a ring style request. The\n" +
            "  implicitly created interceptor will extract the request from the\n" +
            "  context it receives, pass it to the defined function, and then\n" +
            "  associate the return value from the defined function as into\n" +
            "  context with the :response key and return context, e.g.:\n" +
            "\n" +
            "  (defhandler hello-name\n" +
            "    [request]\n" +
            "    (ring.util.response/response\n" +
            "      (str \\\"Hello, \\\" (-> request\n" +
            "                           :params\n" +
            "                           :name))))\n" +
            "\n" +
            "  This is equivalent to:\n" +
            "\n" +
            "  (defbefore hello-name\n" +
            "    [context]\n" +
            "    (let [request (:request context)\n" +
            "          response (ring.util.response/response\n" +
            "                     (str \\\"Hello, \\\" (-> request\n" +
            "                                          :params\n" +
            "                                          :name)))]\n" +
            "      (assoc context :response response)))\")\n" +
            "\n" +
            "(defn middleware\n" +
            "  \"Returns an interceptor which calls `f1` on the :request value of\n" +
            "  context during the enter stage, and `f2` on the :response value of\n" +
            "  context during the leave stage.\"\n" +
            "  ([f1 f2]\n" +
            "     (interceptor {:enter (when f1 #(update-in % [:request] f1))\n" +
            "                   :leave (when f2 #(update-in % [:response] f2))}))\n" +
            "  ([n f1 f2]\n" +
            "     (interceptor {:name (interceptor-name n)\n" +
            "                   :enter (when f1 #(update-in % [:request] f1))\n" +
            "                   :leave (when f2 #(update-in % [:response] f2))})))\n" +
            "\n" +
            "(defmacro defmiddleware\n" +
            "  \"Defines a middleware interceptor. The definition resembles a\n" +
            "  multiple arity function definition, however both fns are\n" +
            "  1-arity. The first fn will be called during the enter stage with the\n" +
            "  value of the :request key in the context, the second during the\n" +
            "  leave stage with the response key in the context, e.g.:\n" +
            "\n" +
            "  (defmiddleware middleware-interceptor\n" +
            "    ([request] (assoc request :middleware :on-request))\n" +
            "    ([response] (assoc response :middleware :on-response)))\"\n" +
            "\n" +
            "  [n & args]\n" +
            "  (let [[docstring args] (if (string? (first args))\n" +
            "                           [(first args) (rest args)]\n" +
            "                           [nil args])\n" +
            "        prefix-list (if docstring\n" +
            "                      (list n docstring)\n" +
            "                      (list n))\n" +
            "        [enter-fn-form args] [(infer-first-interceptor-function args)\n" +
            "                              (infer-rest-interceptor-function args)]\n" +
            "        [leave-fn-form args] [(infer-first-interceptor-function args)\n" +
            "                              (infer-rest-interceptor-function args)]\n" +
            "        interceptor-name# (keyword (name (ns-name *ns*)) (name n))]\n" +
            "    `(definterceptor ~@prefix-list\n" +
            "       (middleware ~interceptor-name# ~enter-fn-form ~leave-fn-form))))\n" +
            "\n";

    static final String httpClj = "; Copyright 2013 Relevance, Inc.\n" +
            "; Copyright 2014-2016 Cognitect, Inc.\n" +
            "\n" +
            "; The use and distribution terms for this software are covered by the\n" +
            "; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)\n" +
            "; which can be found in the file epl-v10.html at the root of this distribution.\n" +
            ";\n" +
            "; By using this software in any fashion, you are agreeing to be bound by\n" +
            "; the terms of this license.\n" +
            ";\n" +
            "; You must not remove this notice, or any other, from this software.\n" +
            "\n" +
            "(ns io.pedestal.http\n" +
            "  \"Namespace which ties all the pedestal components together in a\n" +
            "  sensible default way to make a full blown application.\"\n" +
            "  (:require [io.pedestal.http.route :as route]\n" +
            "            [io.pedestal.http.ring-middlewares :as middlewares]\n" +
            "            [io.pedestal.http.csrf :as csrf]\n" +
            "            [io.pedestal.http.secure-headers :as sec-headers]\n" +
            "            [io.pedestal.interceptor.helpers :as interceptor]\n" +
            "            [io.pedestal.http.servlet :as servlet]\n" +
            "            [io.pedestal.http.impl.servlet-interceptor :as servlet-interceptor]\n" +
            "            [io.pedestal.http.cors :as cors]\n" +
            "            [ring.util.mime-type :as ring-mime]\n" +
            "            [ring.util.response :as ring-response]\n" +
            "            [clojure.string :as string]\n" +
            "            [cheshire.core :as json]\n" +
            "            [cognitect.transit :as transit]\n" +
            "            [io.pedestal.log :as log])\n" +
            "  (:import (java.io OutputStreamWriter\n" +
            "                    OutputStream)))\n" +
            "\n" +
            ";; edn and json response formats\n" +
            "\n" +
            "(defn- print-fn\n" +
            "  [prn-fn]\n" +
            "  (fn [output-stream]\n" +
            "    (with-open [writer (OutputStreamWriter. output-stream)]\n" +
            "      (binding [*out* writer]\n" +
            "        (prn-fn))\n" +
            "      (.flush writer))))\n" +
            "\n" +
            "(defn- data-response\n" +
            "  [f content-type]\n" +
            "  (ring-response/content-type\n" +
            "   (ring-response/response (print-fn f))\n" +
            "   content-type))\n" +
            "\n" +
            "(defn edn-response\n" +
            "  \"Return a Ring response that will print the given `obj` to the HTTP output stream in EDN format.\"\n" +
            "  [obj]\n" +
            "  (data-response #(pr obj) \"application/edn;charset=UTF-8\"))\n" +
            "\n" +
            "(defn json-print\n" +
            "  \"Print object as JSON to *out*\"\n" +
            "  [obj]\n" +
            "  (json/generate-stream obj *out*))\n" +
            "\n" +
            "(defn json-response\n" +
            "  \"Return a Ring response that will print the given `obj` to the HTTP output stream in JSON format.\"\n" +
            "  [obj]\n" +
            "  (data-response #(json-print obj) \"application/json;charset=UTF-8\"))\n" +
            "\n" +
            ";; Interceptors\n" +
            ";; ------------\n" +
            ";; We avoid using the macro-versions in here, to avoid complications with AOT.\n" +
            ";; The error you'd see would be something like,\n" +
            ";;   \"java.lang.IllegalArgumentException:\n" +
            ";;      No matching ctor found for class io.pedestal.interceptor.helpers$after$fn__6188\"\n" +
            ";; Where the macro tries to call a function on 0-arity, but the actual\n" +
            ";; interceptor (already compiled) requires a 2-arity version.\n" +
            "\n" +
            "(def log-request\n" +
            "  \"Log the request's method and uri.\"\n" +
            "  (interceptor/on-request\n" +
            "    ::log-request\n" +
            "    (fn [request]\n" +
            "      (log/info :msg (format \"%s %s\"\n" +
            "                             (string/upper-case (name (:request-method request)))\n" +
            "                             (:uri request)))\n" +
            "      (log/meter ::request)\n" +
            "      request)))\n" +
            "\n" +
            "(defn response?\n" +
            "  \"A valid response is any map that includes an integer :status\n" +
            "  value.\"\n" +
            "  [resp]\n" +
            "  (and (map? resp)\n" +
            "       (integer? (:status resp))))\n" +
            "\n" +
            "(def not-found\n" +
            "  \"An interceptor that returns a 404 when routing failed to resolve a route.\"\n" +
            "  (interceptor/after\n" +
            "    ::not-found\n" +
            "    (fn [context]\n" +
            "      (if-not (response? (:response context))\n" +
            "        (do (log/meter ::not-found)\n" +
            "          (assoc context :response (ring-response/not-found \"Not Found\")))\n" +
            "        context))))\n" +
            "\n" +
            "(def html-body\n" +
            "  \"Set the Content-Type header to \\\"text/html\\\" if the body is a string and a\n" +
            "  type has not been set.\"\n" +
            "  (interceptor/on-response\n" +
            "    ::html-body\n" +
            "    (fn [response]\n" +
            "      (let [body (:body response)\n" +
            "            content-type (get-in response [:headers \"Content-Type\"])]\n" +
            "        (if (and (string? body) (not content-type))\n" +
            "          (ring-response/content-type response \"text/html;charset=UTF-8\")\n" +
            "          response)))))\n" +
            "\n" +
            "(def json-body\n" +
            "  \"Set the Content-Type header to \\\"application/json\\\" and convert the body to\n" +
            "  JSON if the body is a collection and a type has not been set.\"\n" +
            "  (interceptor/on-response\n" +
            "    ::json-body\n" +
            "    (fn [response]\n" +
            "      (let [body (:body response)\n" +
            "            content-type (get-in response [:headers \"Content-Type\"])]\n" +
            "        (if (and (coll? body) (not content-type))\n" +
            "          (-> response\n" +
            "              (ring-response/content-type \"application/json;charset=UTF-8\")\n" +
            "              (assoc :body (print-fn #(json-print body))))\n" +
            "          response)))))\n" +
            "\n" +
            "(defn transit-body-interceptor\n" +
            "  \"Returns an interceptor which sets the Content-Type header to the\n" +
            "  appropriate value depending on the transit format. Converts the body\n" +
            "  to the specified Transit format if the body is a collection and a\n" +
            "  type has not been set. Optionally accepts transit-opts which are\n" +
            "  handed to trasit/writer and may contain custom write handlers.\n" +
            "  \n" +
            "  Expects the following arguments:\n" +
            " \n" +
            "  iname                - namespaced keyword for the interceptor name\n" +
            "  default-content-type - content-type string to set in the response\n" +
            "  transit-format       - either :json or :msgpack\n" +
            "  transit-options      - optional. map of options for transit/writer\"\n" +
            "  ([iname default-content-type transit-format]\n" +
            "   (transit-body-interceptor iname default-content-type transit-format {}))\n" +
            "  \n" +
            "  ([iname default-content-type transit-format transit-opts]\n" +
            "   (interceptor/on-response\n" +
            "    iname \n" +
            "    (fn [response]\n" +
            "      (let [body (:body response)\n" +
            "            content-type (get-in response [:headers \"Content-Type\"])]\n" +
            "        (if (and (coll? body) (not content-type))\n" +
            "          (-> response\n" +
            "              (ring-response/content-type default-content-type)\n" +
            "              (assoc :body (fn [^OutputStream output-stream]\n" +
            "                             (transit/write\n" +
            "                              (transit/writer output-stream transit-format transit-opts) body)\n" +
            "                             (.flush output-stream))))\n" +
            "          response))))))\n" +
            "\n" +
            "(def transit-json-body\n" +
            "  \"Set the Content-Type header to \\\"application/transit+json\\\" and convert the body to\n" +
            "  transit+json if the body is a collection and a type has not been set.\"\n" +
            "  (transit-body-interceptor\n" +
            "   ::transit-json-body\n" +
            "   \"application/transit+json;charset=UTF-8\"\n" +
            "   :json))\n" +
            "\n" +
            "(def transit-msgpack-body\n" +
            "  \"Set the Content-Type header to \\\"application/transit+msgpack\\\" and convert the body to\n" +
            "  transit+msgpack if the body is a collection and a type has not been set.\"\n" +
            "  (transit-body-interceptor\n" +
            "   ::transit-msgpack-body\n" +
            "   \"application/transit+msgpack;charset=UTF-8\"\n" +
            "   :msgpack))\n" +
            "\n" +
            "(def transit-body\n" +
            "  \"Same as `transit-json-body` --\n" +
            "  Set the Content-Type header to \\\"application/transit+json\\\" and convert the body to\n" +
            "  transit+json if the body is a collection and a type has not been set.\"\n" +
            "  transit-json-body)\n" +
            "\n" +
            "(defn default-interceptors\n" +
            "  \"Builds interceptors given an options map with keyword keys prefixed by namespace e.g.\n" +
            "  :io.pedestal.http/routes or ::bootstrap/routes if the namespace is aliased to bootstrap.\n" +
            "\n" +
            "  Note:\n" +
            "    No additional interceptors are added if :interceptors key is set.\n" +
            "\n" +
            "  Options:\n" +
            "\n" +
            "  * :routes: Something that satisfies the io.pedestal.http.route/ExpandableRoutes protocol\n" +
            "    a function that returns routes when called, or a seq of route maps that defines a service's routes.\n" +
            "    If passing in a seq of route maps, it's recommended to use io.pedestal.http.route/expand-routes.\n" +
            "  * :router: The router implementation to to use. Can be :linear-search, :map-tree\n" +
            "    :prefix-tree, or a custom Router constructor function. Defaults to :map-tree, which fallsback on :prefix-tree\n" +
            "  * :file-path: File path used as root by the middlewares/file interceptor. If nil, this interceptor\n" +
            "    is not added. Default is nil.\n" +
            "  * :resource-path: File path used as root by the middlewares/resource interceptor. If nil, this interceptor\n" +
            "    is not added. Default is nil.\n" +
            "  * :method-param-name: Query string parameter used to set the current HTTP verb. Default is _method.\n" +
            "  * :allowed-origins: Determines what origins are allowed for the cors/allow-origin interceptor. If\n" +
            "     nil, this interceptor is not added. Default is nil.\n" +
            "  * :not-found-interceptor: Interceptor to use when returning a not found response. Default is\n" +
            "     the not-found interceptor.\n" +
            "  * :mime-types: Mime-types map used by the middlewares/content-type interceptor. Default is {}.\n" +
            "  * :enable-session: A settings map to include the session middleware interceptor. If nil, this interceptor\n" +
            "     is not added.  Default is nil.\n" +
            "  * :enable-csrf: A settings map to include the csrf-protection interceptor. This implies\n" +
            "     sessions are enabled. If nil, this interceptor is not added. Default is nil.\n" +
            "  * :secure-headers: A settings map for various secure headers.\n" +
            "     Keys are: [:hsts-settings :frame-options-settings :content-type-settings :xss-protection-settings]\n" +
            "     If nil, this interceptor is not added.  Default is the default secure-headers settings\"\n" +
            "  [service-map]\n" +
            "  (let [{interceptors ::interceptors\n" +
            "         routes ::routes\n" +
            "         router ::router\n" +
            "         file-path ::file-path\n" +
            "         resource-path ::resource-path\n" +
            "         method-param-name ::method-param-name\n" +
            "         allowed-origins ::allowed-origins\n" +
            "         not-found-interceptor ::not-found-interceptor\n" +
            "         ext-mime-types ::mime-types\n" +
            "         enable-session ::enable-session\n" +
            "         enable-csrf ::enable-csrf\n" +
            "         secure-headers ::secure-headers\n" +
            "         :or {file-path nil\n" +
            "              router :map-tree\n" +
            "              resource-path nil\n" +
            "              not-found-interceptor not-found\n" +
            "              method-param-name :_method\n" +
            "              ext-mime-types {}\n" +
            "              enable-session nil\n" +
            "              enable-csrf nil\n" +
            "              secure-headers {}}} service-map\n" +
            "        processed-routes (cond\n" +
            "                           (satisfies? route/ExpandableRoutes routes) (route/expand-routes routes)\n" +
            "                           (fn? routes) routes\n" +
            "                           (nil? routes) nil\n" +
            "                           (and (seq? routes) (every? map? routes)) routes\n" +
            "                           :else (throw (ex-info \"Routes specified in the service map don't fulfill the contract.\n" +
            "                                                 They must be a seq of full-route maps or satisfy the ExpandableRoutes protocol\"\n" +
            "                                                 {:routes routes})))]\n" +
            "    (if-not interceptors\n" +
            "      (assoc service-map ::interceptors\n" +
            "             (cond-> []\n" +
            "                     true (conj log-request)\n" +
            "                     (not (nil? allowed-origins)) (conj (cors/allow-origin allowed-origins))\n" +
            "                     true (conj not-found-interceptor)\n" +
            "                     (or enable-session enable-csrf) (conj (middlewares/session (or enable-session {})))\n" +
            "                     enable-csrf (conj (csrf/anti-forgery enable-csrf))\n" +
            "                     true (conj (middlewares/content-type {:mime-types ext-mime-types}))\n" +
            "                     true (conj route/query-params)\n" +
            "                     true (conj (route/method-param method-param-name))\n" +
            "                     ;; TODO: If all platforms support async/NIO responses, we can bring this back\n" +
            "                     ;(not (nil? resource-path)) (conj (middlewares/fast-resource resource-path))\n" +
            "                     (not (nil? resource-path)) (conj (middlewares/resource resource-path))\n" +
            "                     (not (nil? file-path)) (conj (middlewares/file file-path))\n" +
            "                     (not (nil? secure-headers)) (conj (sec-headers/secure-headers secure-headers))\n" +
            "                     true (conj (route/router processed-routes router))))\n" +
            "      service-map)))\n" +
            "\n" +
            "(defn dev-interceptors\n" +
            "  [service-map]\n" +
            "  (update-in service-map [::interceptors]\n" +
            "             #(vec (->> %\n" +
            "                        (cons cors/dev-allow-origin)\n" +
            "                        (cons servlet-interceptor/exception-debug)))))\n" +
            "\n" +
            ";; TODO: Make the next three functions a provider\n" +
            "(defn service-fn\n" +
            "  [{interceptors ::interceptors\n" +
            "    :as service-map}]\n" +
            "  (assoc service-map ::service-fn\n" +
            "         (servlet-interceptor/http-interceptor-service-fn interceptors)))\n" +
            "\n" +
            "(defn servlet\n" +
            "  [{service-fn ::service-fn\n" +
            "    :as service-map}]\n" +
            "  (assoc service-map ::servlet\n" +
            "         (servlet/servlet :service service-fn)))\n" +
            "\n" +
            "(defn create-servlet\n" +
            "  \"Creates a servlet given an options map with keyword keys prefixed by namespace e.g.\n" +
            "  :io.pedestal.http/interceptors or ::bootstrap/interceptors if the namespace is aliased to bootstrap.\n" +
            "\n" +
            "  Options:\n" +
            "\n" +
            "  * :io.pedestal.http/interceptors: A vector of interceptors that defines a service.\n" +
            "\n" +
            "  Note: Additional options are passed to default-interceptors if :interceptors is not set.\"\n" +
            "  [service-map]\n" +
            "  (-> service-map\n" +
            "      default-interceptors\n" +
            "      service-fn\n" +
            "      servlet))\n" +
            "\n" +
            ";;TODO: Make this a multimethod\n" +
            "(defn interceptor-chain-provider\n" +
            "  [service-map]\n" +
            "  (let [provider (cond\n" +
            "                   (fn? (::chain-provider service-map)) (::chain-provider service-map)\n" +
            "                   (keyword? (::type service-map)) (comp servlet service-fn)\n" +
            "                   :else (throw (IllegalArgumentException. \"There was no provider or server type specified.\n" +
            "                                                           Unable to create/connect interceptor chain foundation.\n" +
            "                                                           Try setting :type to :jetty in your service map.\")))]\n" +
            "    (provider service-map)))\n" +
            "\n" +
            "(defn create-provider\n" +
            "  \"Creates the base Interceptor Chain provider, connecting a backend to the interceptor\n" +
            "  chain.\"\n" +
            "  [service-map]\n" +
            "  (-> service-map\n" +
            "      default-interceptors\n" +
            "      interceptor-chain-provider))\n" +
            "\n" +
            "(defn- service-map->server-options\n" +
            "  [service-map]\n" +
            "  (let [server-keys [::host ::port ::join? ::container-options]]\n" +
            "    (into {} (map (fn [[k v]] [(keyword (name k)) v]) (select-keys service-map server-keys)))))\n" +
            "\n" +
            "(defn- server-map->service-map\n" +
            "  [server-map]\n" +
            "  (into {} (map (fn [[k v]] [(keyword \"io.pedestal.http\" (name k)) v]) server-map)))\n" +
            "\n" +
            "(defn server\n" +
            "  [service-map]\n" +
            "  (let [{type ::type\n" +
            "         :or {type :jetty}} service-map\n" +
            "        server-fn (if (fn? type)\n" +
            "                    type\n" +
            "                    (let [server-ns (symbol (str \"io.pedestal.http.\" (name type)))]\n" +
            "                      (require server-ns)\n" +
            "                      (resolve (symbol (name server-ns) \"server\"))))\n" +
            "        server-map (server-fn service-map (service-map->server-options service-map))]\n" +
            "    (when (= type :jetty)\n" +
            "      ;; Load in container optimizations (NIO)\n" +
            "      (require 'io.pedestal.http.jetty.container))\n" +
            "    (when (= type :immutant)\n" +
            "      ;; Load in container optimizations (NIO)\n" +
            "      (require 'io.pedestal.http.immutant.container))\n" +
            "    (merge service-map (server-map->service-map server-map))))\n" +
            "\n" +
            "(defn create-server\n" +
            "  ([service-map]\n" +
            "   (create-server service-map log/maybe-init-java-util-log))\n" +
            "  ([service-map init-fn]\n" +
            "   (init-fn)\n" +
            "   (-> service-map\n" +
            "      create-provider ;; Creates/connects a backend to the interceptor chain\n" +
            "      server)))\n" +
            "\n" +
            "(defn start [service-map]\n" +
            "  ((::start-fn service-map))\n" +
            "  service-map)\n" +
            "\n" +
            "(defn stop [service-map]\n" +
            "  ((::stop-fn service-map))\n" +
            "  service-map)\n" +
            "\n" +
            ";; Container prod mode for use with the io.pedestal.servlet.ClojureVarServlet class.\n" +
            "\n" +
            "(defn servlet-init\n" +
            "  [service config]\n" +
            "  (let [service (create-servlet service)]\n" +
            "    (.init ^javax.servlet.Servlet (::servlet service) config)\n" +
            "    service))\n" +
            "\n" +
            "(defn servlet-destroy [service]\n" +
            "  (dissoc service ::servlet))\n" +
            "\n" +
            "(defn servlet-service [service servlet-req servlet-resp]\n" +
            "  (.service ^javax.servlet.Servlet (::servlet service) servlet-req servlet-resp))\n" +
            "\n";

    @Test(timeout=100L)
    public void Test_lex_pedestal_interceptor_helpers() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexable l = Lexable.create("helpers.clj", pedestalInterceptorHelpers, new CljLex(), q);

        l.run();

        final long countLeft = q.stream().filter(item -> ((Item)item).type == itemLeftParen).count();
        final long countRight = q.stream().filter(item -> ((Item)item).type == itemRightParen).count();
        final long countBracketLeft = q.stream().filter(item -> ((Item)item).type == itemLeftBracket).count();
        final long countBracketRight = q.stream().filter(item -> ((Item)item).type == itemRightBracket).count();

        // quick verification that parens are balanced.
        assertThat(countLeft, is(200L));
        assertThat(countRight, is(200L));
        assertThat(countBracketLeft, is(countBracketRight));
    }

    @Test
    public void Test_lex_pedestal_http() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexable l = Lexable.create("http.clj", httpClj, new CljLex(), q);

        l.run();

        final long countLeft = q.stream().filter(item -> ((Item)item).type == itemLeftParen).count();
        final long countRight = q.stream().filter(item -> ((Item)item).type == itemRightParen).count();
        final long countBracketLeft = q.stream().filter(item -> ((Item)item).type == itemLeftBracket).count();
        final long countBracketRight = q.stream().filter(item -> ((Item)item).type == itemRightBracket).count();

        // quick verification that parens are balanced.
        assertThat("left parens", countLeft, is(231L));
        assertThat("right parens", countRight, is(231L));
        assertThat("brackets", countBracketLeft, is(countBracketRight));
    }

    @Test(timeout=100L)
    public void Test_next() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexable l = Lexable.create("test.clj", "a", new CljLex(), q);

        char c1 = l.next();
        char c2 = l.next();

        assertEquals('a', c1);
        assertEquals(3, c2);
    }

    @Test(timeout=100L)
    public void Test_ignore() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexable l = Lexable.create("test.clj", "   ", new CljLex(), q);
        l.acceptRun(" ");
        l.ignore();

        assertEquals(3, l.getPos());
    }

    @Test(timeout=100L)
    public void Test_accept() {
        final ReadWriterQueue q = new ReadWriterQueue();
        final Lexable l = Lexable.create("test.clj", " ) ", new CljLex(), q);
        l.accept(" ");
        l.accept(")");
        l.accept(" ");
        assertEquals(3, l.getPos());
    }

    @Test(timeout=100L)
    public void Test_Lexer_lex_single_line() {
        ReadWriterQueue q = new ReadWriterQueue();
        final Lexable l = Lexable.create("comment.clj",
                "(defn hello [filename] (prn \"Hola \" filename))", new CljLex(), q);

        l.run();

        String tokens = q.stream()
                .filter(item -> ((Item)item).type != itemEOF)
                .map(item -> ((Item)item).val)
                .reduce((a,b) -> a + " " + b)
                .get();

        assertEquals(q.size(), 13);
        assertEquals("( defn hello [ filename ] ( prn \"Hola \" filename ) )", tokens);
    }


    @Test(timeout=100L)
    public void Test_Lexer_lex_multiple_lines() {
        ReadWriterQueue q = new ReadWriterQueue();
        final Lexable l = Lexable.create("comment.clj",
                "(ns my.core)\n\n\n(defn hello [filename]\n (prn \"Hola \" filename))", new CljLex(), q);


        l.run();

        String tokens = q.stream()
                .filter(item -> ((Item)item).type != itemEOF)
                .map(item -> ((Item)item).val)
                .reduce((a,b) -> a + " " + b)
                .get();

        assertEquals(q.size(), 17);
        assertEquals("( ns my.core ) ( defn hello [ filename ] ( prn \"Hola \" filename ) )", tokens);
    }
}