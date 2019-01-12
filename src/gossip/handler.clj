(ns gossip.handler
  (:refer-clojure :exclude [read-string])
  (:require [clojure.edn :refer [read-string]]

            [clojure.data.json :refer [write-str]]
            (compojure [core :refer :all]
                       [route :as route])
            (ring.util [anti-forgery :refer [anti-forgery-field]]
                       [response :refer [redirect-after-post]])
            (ring.middleware [defaults :refer [wrap-defaults site-defaults]])
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            (gossip [db :refer :all]
                    [diff :refer [diff edit-dist]]))
  (:use [hiccup core page element form]))

(defn entry [id req]
  (html5
    (include-css "/style.css")
    [:script (str "var entry_id = " (write-str id) ";")]
    (include-js "/script.js")
    [:div {:id 'edit_dist}]
    [:div {:id 'diff}]
    (form-to {:id 'form} [:post (str "/draft/" (print-str id))]
      (anti-forgery-field)
      (text-area :content (get-entry id))
      (submit-button "save draft")
      (if (friend/authorized? #{::publisher} (friend/identity req))
        (submit-button {:formaction (str "/publish/" (print-str id))} "publish")))))

(defn entry-post [id content public]
  (let [new-id (save-entry content id public)]
    (redirect-after-post (str "/entry/" new-id))))

(defn diff-post [id content]
  (let [d (diff content (get-entry id))]
    (write-str
    {:edit_dist (- 140 (edit-dist d))
     :diff (html
            (for [[type chunk] d]
              [:span {:class (case type
                               :ab 'common
                               :a 'add
                               :b 'del)}
               (h chunk)]))})))

(defn index [req]
  (html5
    (if (friend/authorized? #{::publisher} (friend/identity req))
      (link-to "/entry/nil" "new entry")
      "hoi!")
    (for [[id content] (all-heads true)]
      [:div
       (link-to (str "/entry/" id) id)
       " "
       (h content)])
    (if (friend/authorized? #{::publisher} (friend/identity req))
      [:div
       [:h3 "drafts"]
       (for [[id content] (all-heads false)]
         [:div
          (link-to (str "/entry/" id) id)
          " "
          (h content)])])))


(defn get-login-form [req]
  (html5
    [:h3 "Login"]
    (form-to [:post "/login"]
      (anti-forgery-field)
      [:div "Username" [:input {:type "text" :name "username"}]]
      [:div "Password" [:input {:type "password" :name "password"}]]
      [:div (submit-button "login")])))

(defroutes publisher-routes
  (POST "/publish/:id" [id content] (entry-post (read-string id) content true)))

(defroutes app-routes
  (GET "/login" [] get-login-form)
  (GET "/" [] index)
  (GET "/entry/:id" [id :as req] (entry (read-string id) req))
  (POST "/draft/:id" [id content] (entry-post (read-string id) content false))
  (POST "/diff/:id" [id content] (diff-post (read-string id) content))
  (route/resources "/")
  (route/not-found "Not Found")
  (friend/wrap-authorize publisher-routes #{::publisher}))

(def users (read-string (slurp "users.edn")))

(def app
  (-> app-routes
      #_(wrap-defaults site-defaults)
      (friend/authenticate {:credential-fn (partial creds/bcrypt-credential-fn users)
                            :workflows [(workflows/interactive-form)]})
      (wrap-defaults site-defaults)))

(setup)
