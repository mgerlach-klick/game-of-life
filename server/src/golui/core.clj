(ns golui.core
  (:require [compojure.core :refer :all]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :refer [response]]
            [org.httpkit.server :refer [run-server]]
            [seesaw.core :as s]
            [clojure.pprint :refer [pprint]]
            [ring.middleware.cors :refer [wrap-cors]])
  (:gen-class))

(def *grid {})
(def frame nil)
(def server nil)


(defn setup-ui []
  (s/native!)
  (-> (s/frame :title "http://localhost:5000",
               :on-close :exit)))

(defn make-label [id]
  (let [l (s/label :id id)]
    (s/config! l
               :background :pink
               :opaque? true
               :preferred-size [25 :by 25])))

(defn make-grid [frame w h]
  (let [grid (atom (sorted-map))]
    (doseq [h (range h)
            w (range w)]
      (swap! grid assoc [w h]
             (make-label [w h])))
    (s/config! frame :content (s/grid-panel :columns w :rows h :items (vals @grid)))
    (s/pack! frame)
    (s/show! frame)
    grid))

(defn color-pixel [{x :x y :y color :color}]
  (s/config! (get @*grid [y x]) :background color ))

(defn render [data]
  (doseq [pixel (:cells data)]
    (color-pixel pixel))
  (pprint data)
  (println "------------------------------------")
  (response "ok"))

(defroutes myapp
  (GET "/" [] "Try this:
curl -H \"Content-Type: application/json\" -X POST -d '{\"cells\":[{\"x\":3,\"y\":0,\"color\":\"#ff0000\"}]}' http://localhost:5000/")
  (POST "/" req (render (:body req))))

(def handler
  (-> myapp
      wrap-json-response
      (wrap-json-body {:keywords? true })
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])))

(defn -main []
  (let [grid-size (or (first *command-line-args*) 25)]
    (alter-var-root #'frame (constantly (setup-ui)))
    (alter-var-root #'*grid (constantly (make-grid frame grid-size grid-size)))
    (alter-var-root #'server (constantly (run-server #'handler {:port 5000}))))
  (println "Try this:\n curl -H \"Content-Type: application/json\" -X POST -d '{\"cells\":[{\"x\":3,\"y\":0,\"color\":\"#ff0000\"}]}' http://localhost:5000/"))
