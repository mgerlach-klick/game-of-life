(def request (js/require "request"))
(def random-color (js/require "randomColor"))

(def ^:dynamic *width* 25)
(def ^:dynamic *height* 25)

(defn valid? [cell]
  (let [[x y] cell]
    (and
     (< -1 x *width*)
     (< -1 y *height*))))

(defn neighbour-positions [cell]
  (let [[x y] cell
        neighbours [[(dec x) (dec y)] ;al
                    [x (dec y)] ;a
                    [(inc x) (dec y)] ; ar
                    [(dec x) y] ;l
                    [(inc x) y] ;r
                    [(dec x) (inc y)] ;bl
                    [x (inc y)] ;b
                    [(inc x) (inc y)]]] ;br
    (filter valid? neighbours)))

(defn get-neighbours [world neighbours]
  (map (partial get world) neighbours))

(defn alive? [world cell]
  (true? (get world cell)))

(defn dead? [world cell]
  (false? (get world cell)))

(defn make-random-world []
  (let [world (atom (sorted-map))]
    (doseq [y (range *height*)
            x (range *width*)]
      (swap! world assoc [x y] (rand-nth [true false])))
    @world))

(defn count-alive-neighbours [world cell]
  (->> cell
      (neighbour-positions)
      (get-neighbours world)
      (filter true?)
      count))

;;; Any live cell with fewer than two live neighbours dies, as if caused by underpopulation.
;;; Any live cell with two or three live neighbours lives on to the next generation.
;;; Any live cell with more than three live neighbours dies, as if by overpopulation.
;;; Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.

(defn decide-fate [world cell]
  (let [A (count-alive-neighbours world cell)
        same (get world cell)
        dead false
        alive true]
    (cond
      (< A 2) dead
      (and (<= 2 A 3)
           (alive? world cell)) alive
      (and (> A 3)
           (alive? world cell)) dead
      (and (= A 3)
           (dead? world cell)) alive
      :else same)))

(defn tick [world]
  (let [new-world (atom (sorted-map))]
    (doseq [[cell state] world
            :let [fate (decide-fate world cell)]]
      (prn cell state '=> fate 'because (count-alive-neighbours world cell) 'from (neighbour-positions cell))
      (swap! new-world assoc cell fate))
    @new-world))

(defn create-request [world]
  (let [wall-cell (fn [[[x y] alive]]
                    {:x x
                     :y y
                     :color (if alive
                            (random-color #js {:luminosity "bright"})
                            "#000")})]
    (map wall-cell world)))

(defn make-request [req]
  (let [req-obj (clj->js {:url "http://localhost:5000/"
                          :method "POST"
                          :json {:cells req}})]
    (request req-obj)))


(defn tick-tock [world old]
  (print \.)
  (println (.stringify js/JSON (clj->js world) nil 4))
  (if (= world old)
    (js/setTimeout #(tick-tock (make-random-world) nil) 3000)
    (do
      (-> world
          create-request
          make-request)
      (js/setTimeout #(tick-tock (tick world) world) 1000 ))))

(tick-tock (make-random-world) nil)
