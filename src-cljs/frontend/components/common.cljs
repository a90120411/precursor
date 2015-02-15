(ns frontend.components.common
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close!]]
            [frontend.async :refer [put!]]
            [frontend.datetime :as datetime]
            [frontend.utils :as utils :include-macros true]
            [frontend.utils.github :as gh-utils]
            [goog.dom.DomHelper]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [sablono.core :refer [html]]))

(defn contact-us-inner [controls-ch]
  [:a {:on-click #(put! controls-ch [:intercom-dialog-raised])}
   "contact us"])

(defn flashes
  "Displays common error messages (poorly named since flashes has another use in the app)."
  [error-message owner]
  (reify
    om/IRender
    (render [_]
      (let [controls-ch (om/get-shared owner [:comms :controls])
            ;; use error messages that have html without passing html around
            display-message (condp = error-message
                              :logged-out [:span "You've been logged out, " [:a {:href (gh-utils/auth-url)} "log back in"] " to continue."]
                              error-message)]
        (html
         (if-not error-message
           [:span]

           [:div.flash-error-wrapper.row-fluid
            [:div.offset1.span10
             [:div.alert.alert-block.alert-danger
              [:a.close {:on-click #(put! controls-ch [:clear-error-message-clicked])} "×"]
              "Error: " display-message
              " If we can help, " (contact-us-inner controls-ch) "."]]]))))))

(defn normalize-html
  "Creates a valid html string given a (possibly) invalid html string."
  [html-string]
  (let [dom-helper (goog.dom.DomHelper.)]
    (->> html-string
         (.htmlToDocumentFragment dom-helper)
         (.getOuterHtml dom-helper))))

(defn messages [messages]
  [:div.row-fluid
   (when (pos? (count messages))
     (let [dom-helper (goog.dom.DomHelper.)]
       [:div#build-messages.offset1.span10
        (map (fn [message]
               [:div.alert.alert-info
                [:strong "Warning: "]
                [:span {:dangerouslySetInnerHTML #js {"__html" (normalize-html (:message message))}}]])
             messages)]))])

(def icon-paths {
  :stroke-precursor "M35.7,45.3V95 M60.5,84.4C82,78.6,94.8,56.5,89,34.9C83.2,13.4,61.1,0.6,39.5,6.4S5.2,34.3,11,55.9"
  :stroke-download "M5,95 h90 M5,85v10 M95,85v10 M50,5v70 M50,75l30-30 M20,45l30,30"
  :stroke-check "M35,80 L5,50 M95,20L35,80"
  :stroke-times "M82.5,82.5l-65-65 M82.5,17.5l-65,65"
  :stroke-cursor "M23.3,80.4V5 l53.3,53.3c0,0-21.5,0-21.5,0s12.4,29.8,12.4,29.8L50.9,95c0,0-12.4-29.8-12.4-29.8S23.3,80.4,23.3,80.4z"
  :stroke-rectangle "M87.5,87.5h-75v-75h75V87.5z M20,5H5v15h15V5z M95,5H80v15h15V5z M20,80H5v15h15V80z M95,80H80v15h15V80z"
  :stroke-line "M95,20H80V5h15V20z M20,80H5v15h15V80z M87.5,12.5l-75,75"
  :stroke-text "M65.9,92.4H34.1H50 V7.6 M95,21.4c0,0-7.9-13.8-7.9-13.8c0,0-74.1,0-74.1,0L5,21.4"
  :stroke-pencil "M89.5,10.5c3.9,3.9,6.3,7.8,5.3,8.8L24.3,89.8L5,95l5.2-19.3L80.7,5.2C81.7,4.2,85.6,6.6,89.5,10.5z M22.5,88.1L11.9,77.5 M81.3,8.1c0.9,1.7,2.6,3.8,4.7,5.9c2.1,2.1,4.2,3.8,5.9,4.7 M70.3,19.1c0.9,1.7,2.6,3.8,4.7,5.9c2.1,2.1,4.2,3.8,5.9,4.7 M68.3,21.1c0.9,1.7,2.6,3.8,4.7,5.9c2.1,2.1,4.2,3.8,5.9,4.7"
  :stroke-ellipse "M57.5,5h-15v15h15V5z M95,42.5H80v15h15V42.5z M20,42.5H5v15h15V42.5z M57.5,80h-15v15h15V80z M87.5,50c0,20.7-16.8,37.5-37.5,37.5 S12.5,70.7,12.5,50S29.3,12.5,50,12.5S87.5,29.3,87.5,50z"
  :stroke-user "M50,5 c11.8,0,21.3,9.5,21.3,21.3S61.8,47.6,50,47.6s-21.3-9.5-21.3-21.3S38.2,5,50,5z M24.8,45.9C17,52.4,12,64.1,12,71.9 C12,86.4,29,95,50,95c21,0,38-8.6,38-23.1c0-7.8-4.9-19.5-12.7-26"
  :stroke-users "M41.9,13.3 c10.5,0,19,8.5,19,19s-8.5,19-19,19s-19-8.5-19-19S31.4,13.3,41.9,13.3z M68.4,41.5C76.9,39.6,83.2,32,83.2,23c0-10.5-8.5-19-19-19 C60.5,4,57,5.1,54.1,6.9 M19.4,49.7C12.4,55.5,8,66,8,72.9c0,12.9,15.1,20.6,33.8,20.6c18.7,0,33.8-7.7,33.8-20.6 c0-6.9-4.4-17.4-11.4-23.2 M85.9,80C93.3,76.4,98,70.9,98,63.7c0-6.9-4.4-17.4-11.4-23.2"
  :stroke-dot "M49.5,50a0.5,0.5 0 1,0 1,0a0.5,0.5 0 1,0 -1,0"
  :stroke-chats "M60.8,63.2c-0.2,12-12.9,21.5-28.3,21.2 c-3.5-0.1-6.8-0.6-9.8-1.6L8.5,88.8c0,0,4.8-9.7,0.4-15.3c-2.6-3.4-4-7.3-3.9-11.4c0.2-12,12.9-21.5,28.3-21.2 C48.7,41.2,61,51.2,60.8,63.2z M68.5,73.9L89.7,83c0,0-7.2-14.5-0.6-22.9c3.8-5,6-10.9,5.9-17.2c-0.4-18-19.4-32.2-42.5-31.7 c-19.1,0.4-35,10.7-39.8,24.4"
  :stroke-crosshair "M50,5v90 M5,50h90"
  :stroke-ibeam "M50,10v80 M41.3,95h-10 M68.7,95h-10 M45,50h10 M50,90 l-8.7,5 M50,90l8.7,5 M58.7,5h10 M31.3,5h10 M58.7,5L50,10 M41.3,5l8.7,5"
  :stroke-blog "M85,32.5H35 M35,50h50 M35,67.5h50 M25,85h60 c5.5,0,10-4.5,10-10V15H25v60c0,5.5-4.5,10-10,10S5,80.5,5,75V35h15"
  :stroke-clock "M95,50c0,24.9-20.1,45-45,45S5,74.9,5,50S25.1,5,50,5 S95,25.1,95,50z M71.2,71.2C71.2,71.2,50,50,50,50V20"
  :stroke-delete "M35,80L5,50c0,0,30-30,30-30s60,0,60,0v60 C95,80,35,80,35,80z M45,35l30,30 M75,35L45,65"
  :stroke-shift "M5,52L50,7l45,45H70.5V93H29.5V52H5z"
  :stroke-option "M95,15H65 M95,85H75L35,15H5"
  :stroke-control "M86,41L50,5L14,41"
  :stroke-command "M65,35v30H35V35H65z M20,5C11.7,5,5,11.7,5,20 c0,8.3,6.7,15,15,15h15V20C35,11.7,28.3,5,20,5z M95,20c0-8.3-6.7-15-15-15c-8.3,0-15,6.7-15,15v15h15C88.3,35,95,28.3,95,20z M5,80c0,8.3,6.7,15,15,15c8.3,0,15-6.7,15-15V65H20C11.7,65,5,71.7,5,80z M80,65H65v15c0,8.3,6.7,15,15,15c8.3,0,15-6.7,15-15 C95,71.7,88.3,65,80,65z"
  :stroke-esc "M95,46.6c-3.2-3.2-7.4-4.8-11.7-4.8 c-9.1,0-16.5,7.4-16.5,16.5s7.4,16.5,16.5,16.5c4.2,0,8.4-1.6,11.7-4.8 M40.4,72.3c1.6,1.6,3.7,2.4,5.8,2.4c4.6,0,8.2-3.7,8.2-8.2 s-3.7-8.2-8.2-8.2S38,54.6,38,50s3.7-8.2,8.2-8.2c2.1,0,4.2,0.8,5.8,2.4 M29.7,74.7H5c0,0,0-49.5,0-49.5h24.7 M5,50h18.6"
  :stroke-scroll "M65,20L50,5L35,20 M65,35L50,20L35,35 M35,80l15,15 l15-15 M35,65l15,15l15-15 M35,50l15,15l15-15c0,0-15-15-15-15S35,50,35,50z"
  :stroke-mouse "M58,5h17c2.8,0,5,2.2,5,5v55c0,16.6-13.4,30-30,30 S20,81.6,20,65V10c0-2.8,2.2-5,5-5h20c2.8,0,5,2.2,5,5c0,0,0,25,0,25"
  :stroke-globe "M95,50c0,24.9-20.1,45-45,45S5,74.9,5,50S25.1,5,50,5 S95,25.1,95,50z M16.5,20c8.2,4.6,20.2,7.5,33.5,7.5s25.3-2.9,33.5-7.5 M83.5,80c-8.2-4.6-20.2-7.5-33.5-7.5S24.7,75.4,16.5,80 M50,5C37.6,5,27.5,25.1,27.5,50S37.6,95,50,95s22.5-20.1,22.5-45S62.4,5,50,5z M5,50h90 M50,95V5"
  :stroke-newdoc "M58,80V50 M43,65h30 M12,77l0-52L32,5l34,0v20 M58,35 c-16.6,0-30,13.4-30,30s13.4,30,30,30s30-13.4,30-30S74.6,35,58,35z M12,25h20V5"
  :stroke-login "M35,82.1V70 M5,43.6h42.9 M30.7,60.7l17.1-17.1 L30.7,26.4 M35,17.1V5h60c0,0,0,77.1,0,77.1L52.1,95V17.9L88.3,7"
  :stroke-logout "M5,82.1 M65,55v27.1 M95,43.6H52.1 M77.9,60.7L95,43.6 L77.9,26.4 M65,32.1V5H5c0,0,0,77.1,0,77.1L47.9,95V17.9L11.5,7"
  :stroke-info "M50,40v35 M59,66l-9,9 M41,49l9-9 M50,25 c-1.4,0-2.5,1.1-2.5,2.5S48.6,30,50,30s2.5-1.1,2.5-2.5S51.4,25,50,25z M95,50c0,24.9-20.1,45-45,45S5,74.9,5,50S25.1,5,50,5 S95,25.1,95,50z"
  :stroke-cog "M94.8,54.3c-0.3,2.1-1.9,3.8-3.9,4c-2.5,0.3-7.7,0.9-7.7,0.9c-2.3,0.5-3.9,2.5-3.9,4.9c0,1,0.3,2,0.8,2.7c0,0.1,3.1,4.1,4.7,6.2 c1.3,1.6,1.2,3.9-0.1,5.5c-1.8,2.3-3.8,4.3-6.1,6.1c-0.8,0.7-1.8,1-2.8,1c-0.9,0-2-0.3-2.7-0.9L67,80.1c-0.7-0.6-1.8-0.8-2.8-0.8 c-2.4,0-4.4,1.8-4.9,4.1l-0.9,7.5c-0.3,2.1-2,3.7-4,3.9C52.9,94.9,51.4,95,50,95c-1.4,0-2.9-0.1-4.3-0.2c-2.1-0.3-3.7-1.9-4-3.9 c0,0-0.9-7.4-0.9-7.5c-0.4-2.3-2.4-4.1-4.9-4.1c-1.1,0-2.2,0.4-3,0.9L27,84.8c-0.7,0.7-1.8,0.9-2.7,0.9c-1,0-2-0.4-2.8-1 c-2.3-1.8-4.3-3.8-6.1-6.1c-1.3-1.6-1.4-3.9-0.1-5.5l4.5-5.9c0.7-0.8,1-1.9,1-3c0-2.5-1.9-4.6-4.3-4.9l-7.3-0.9 c-2.1-0.3-3.7-2-3.9-4c-0.3-2.8-0.3-5.7,0-8.6c0.2-2.1,1.9-3.7,3.9-4l7.3-0.9c2.4-0.4,4.3-2.4,4.3-5c0-1-0.4-2.1-1-2.9 c0,0-3-3.9-4.5-5.9c-1.3-1.6-1.3-3.9,0.1-5.5c1.8-2.3,3.8-4.3,6.1-6.1c1.6-1.3,3.9-1.4,5.5-0.1l5.9,4.6c0.8,0.6,1.9,0.9,3,0.9 c2.4,0,4.5-1.8,4.9-4.1l0.9-7.5c0.3-2.1,2-3.7,4-3.9c2.8-0.3,5.7-0.3,8.6,0c2.1,0.3,3.7,1.9,4,3.9l0.9,7.5c0.5,2.3,2.4,4.1,4.9,4.1 c1,0,2-0.4,2.8-0.8c0,0,4-3.1,6.1-4.7c1.6-1.3,3.9-1.2,5.5,0.1c2.3,1.8,4.3,3.8,6.1,6.1c1.3,1.6,1.4,3.9,0.1,5.5 c0,0-4.7,6.1-4.7,6.2c-0.6,0.7-0.8,1.7-0.8,2.6c0,2.4,1.7,4.4,3.9,5c0,0,5.2,0.7,7.7,0.9c2.1,0.3,3.7,2,3.9,4 C95.1,48.5,95.1,51.4,94.8,54.3z"
  :stroke-arrow-up "M50,95V5 M86,41L50,5L14,41"
  :stroke-arrow-down "M50,95V5 M14,59l36,36l36-36"
  :stroke-arrow-right "M95,50H5 M59,86l36-36L59,14"
  :stroke-arrow-left "M95,50H5 M41,14L5,50l36,36"
  :stroke-menu-top "M5,25h90"
  :stroke-menu-mid "M5,50h90"
  :stroke-menu-btm "M5,75h90"
  :stroke-lock-top "M75,45V30C75,16.2,63.8,5,50,5S25,16.2,25,30v15"
  :stroke-chat "M50,5c24.9,0,45,20.1,45,45c0,24.9-20.1,45-45,45H5V50 C5,25.1,25.1,5,50,5z"
  :stroke-at "M70,50c0,11-9,20-20,20s-20-9-20-20s9-20,20-20 S70,39,70,50z M70,25v50 M90.3,70c3-6,4.7-12.8,4.7-20C95,25.1,74.9,5,50,5S5,25.1,5,50c0,24.9,20.1,45,45,45c7.2,0,14-1.7,20-4.7"
  :stroke-share "M50,5h45c0,0,0,45,0,45 M50,50L95,5 M27.5,5H5v90 c0,0,90,0,90,0V72.5"
  :stroke-phone "M70,95 H30c-5.5,0-10-4.5-10-10V15c0-5.5,4.5-10,10-10h40c5.5,0,10,4.5,10,10v70C80,90.5,75.5,95,70,95z M50.8,70.1 c-3.4-0.5-6.2,2.4-5.7,5.7c0.3,2.1,2,3.8,4.1,4.1c3.4,0.5,6.2-2.4,5.7-5.7C54.6,72.1,52.9,70.4,50.8,70.1z M65,20H35v35h30V20z"

  :fill-twitter "M100,19c-3.7,1.6-7.6,2.7-11.8,3.2c4.2-2.5,7.5-6.6,9-11.4c-4,2.4-8.4,4.1-13,5c-3.7-4-9.1-6.5-15-6.5 c-11.3,0-20.5,9.2-20.5,20.5c0,1.6,0.2,3.2,0.5,4.7c-17.1-0.9-32.2-9-42.3-21.4c-1.8,3-2.8,6.6-2.8,10.3c0,7.1,3.6,13.4,9.1,17.1 c-3.4-0.1-6.5-1-9.3-2.6c0,0.1,0,0.2,0,0.3c0,9.9,7.1,18.2,16.5,20.1c-1.7,0.5-3.5,0.7-5.4,0.7c-1.3,0-2.6-0.1-3.9-0.4 c2.6,8.2,10.2,14.1,19.2,14.2c-7,5.5-15.9,8.8-25.5,8.8c-1.7,0-3.3-0.1-4.9-0.3c9.1,5.8,19.9,9.2,31.4,9.2 c37.7,0,58.4-31.3,58.4-58.4c0-0.9,0-1.8-0.1-2.7C93.8,26.7,97.2,23.1,100,19z"
  :fill-google "M53.8,0C35.5,0,25.6,11.6,25.6,24.5c0,9.8,7.1,21,21.6,21h3.7c0,0-1,2.4-1,4.8c0,3.5,1.2,5.4,3.9,8.4 c-25,1.5-35.1,11.6-35.1,22.5c0,9.5,9.1,18.9,28.2,18.9c22.6,0,34.4-12.6,34.4-24.9c0-8.7-4.3-13.5-15.3-21.7 c-3.2-2.5-3.9-4.1-3.9-6c0-2.7,1.6-4.5,2.2-5.1c1-1.1,2.8-2.3,3.5-2.9c3.7-3.1,8.9-7.7,8.9-17c0-6.3-2.6-11.8-8.6-16.9h7.3L80.9,0 L53.8,0L53.8,0z M48.8,4.1c3.3,0,6.1,1.2,9,3.6c3.2,2.9,8.4,10.8,8.4,20.5c0,10.5-8.2,13.4-12.6,13.4c-2.2,0-4.8-0.6-6.9-2.1 C41.8,36.4,37,28,37,17.9C37,8.9,42.4,4.1,48.8,4.1z M56,62.7c1.4,0,2.4,0.1,2.4,0.1s3.3,2.4,5.6,4.1c5.4,4.2,8.7,7.5,8.7,13.2 c0,7.9-7.4,14.1-19.3,14.1c-13.1,0-23.1-6.1-23.1-16C30.4,70,37.2,62.9,56,62.7L56,62.7z"
  :fill-north "M30,20L50,0l20,20H30z"
  :fill-south "M70,80l-20,20L30,80H70z"
  :fill-east "M80,30l20,20L80,70V30z"
  :fill-west "M20,70L0,50l20-20V70z"
  :fill-up-down "M55,80H45V20h10V80z"
  :fill-left-right "M80,55H20V45h60V55z"
  :fill-lock-bottom "M87.5,95h-75V45h75V95z"
  :fill-chat1 "M32.5,50c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S32.5,45.9,32.5,50z"
  :fill-chat2 "M82.5,50c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S82.5,45.9,82.5,50z"
  :fill-x1 "M32.5,25c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S32.5,20.9,32.5,25z"
  :fill-x2 "M82.5,25c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S82.5,20.9,82.5,25z"
  :fill-x3 "M45,37.5c0,4.1-3.4,7.5-7.5,7.5S30,41.6,30,37.5s3.4-7.5,7.5-7.5S45,33.4,45,37.5z"
  :fill-x4 "M70,37.5c0,4.1-3.4,7.5-7.5,7.5S55,41.6,55,37.5s3.4-7.5,7.5-7.5S70,33.4,70,37.5z"
  :fill-x5 "M57.5,50c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S57.5,45.9,57.5,50z"
  :fill-x6 "M45,62.5c0,4.1-3.4,7.5-7.5,7.5S30,66.6,30,62.5s3.4-7.5,7.5-7.5S45,58.4,45,62.5z"
  :fill-x7 "M70,62.5c0,4.1-3.4,7.5-7.5,7.5S55,66.6,55,62.5s3.4-7.5,7.5-7.5S70,58.4,70,62.5z"
  :fill-x8 "M32.5,75c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S32.5,70.9,32.5,75z"
  :fill-x9 "M82.5,75c0,4.1-3.4,7.5-7.5,7.5s-7.5-3.4-7.5-7.5s3.4-7.5,7.5-7.5S82.5,70.9,82.5,75z"
})

(def icon-templates {
  :precursor   {:paths [:stroke-precursor]}
  :download    {:paths [:stroke-download]}
  :check       {:paths [:stroke-check]}
  :times       {:paths [:stroke-times]}
  :cursor      {:paths [:stroke-cursor]}
  :rectangle   {:paths [:stroke-rectangle]}
  :line        {:paths [:stroke-line]}
  :text        {:paths [:stroke-text]}
  :pencil      {:paths [:stroke-pencil]}
  :ellipse     {:paths [:stroke-ellipse]}
  :user        {:paths [:stroke-user]}
  :users       {:paths [:stroke-users]}
  :bullet      {:paths [:stroke-dot]}
  :chats       {:paths [:stroke-chats]}
  :crosshair   {:paths [:stroke-crosshair]}
  :ibeam       {:paths [:stroke-ibeam]}
  :blog        {:paths [:stroke-blog]}
  :clock       {:paths [:stroke-clock]}
  :delete      {:paths [:stroke-delete]}
  :shift       {:paths [:stroke-shift]}
  :option      {:paths [:stroke-option]}
  :control     {:paths [:stroke-control]}
  :command     {:paths [:stroke-command]}
  :esc         {:paths [:stroke-esc]}
  :scroll      {:paths [:stroke-scroll]}
  :mouse       {:paths [:stroke-mouse]}
  :globe       {:paths [:stroke-globe]}
  :newdoc      {:paths [:stroke-newdoc]}
  :login       {:paths [:stroke-login]}
  :logout      {:paths [:stroke-logout]}
  :info        {:paths [:stroke-info]}
  :arrow-up    {:paths [:stroke-arrow-up]}
  :arrow-down  {:paths [:stroke-arrow-down]}
  :arrow-left  {:paths [:stroke-arrow-left]}
  :arrow-right {:paths [:stroke-arrow-right]}
  :at          {:paths [:stroke-at]}
  :share       {:paths [:stroke-share]}
  :phone       {:paths [:stroke-phone]}
  :settings    {:paths [:stroke-dot
                        :stroke-cog]}
  :menu        {:paths [:stroke-menu-top
                        :stroke-menu-mid
                        :stroke-menu-btm]}

  :lock        {:paths [:stroke-lock-top
                        :fill-lock-bottom]}
  :chat        {:paths [:stroke-chat
                        :fill-chat1
                        :fill-x5
                        :fill-chat2]}
  :chat-morph  {:paths [:stroke-chat
                        :fill-x1
                        :fill-x2
                        :fill-x3
                        :fill-x4
                        :fill-x5
                        :fill-x6
                        :fill-x7
                        :fill-x8
                        :fill-x9]}

  :twitter     {:paths [:fill-twitter]}
  :google      {:paths [:fill-google]}
  :north       {:paths [:fill-up-down
                        :fill-north]}
  :south       {:paths [:fill-up-down
                        :fill-south]}
  :east        {:paths [:fill-left-right
                        :fill-east]}
  :west        {:paths [:fill-left-right
                        :fill-west]}
  :north-south {:paths [:fill-up-down
                        :fill-north
                        :fill-south]}
  :east-west   {:paths [:fill-left-right
                        :fill-east
                        :fill-west]}
})

(defn svg-icon [icon-name & [{:keys [path-props svg-props]}]]
  (let [template (get icon-templates icon-name)]
    [:svg (merge {:viewBox "0 0 100 100" :class "iconpile"} svg-props)
     (for [path (:paths template)]
       [:path (merge {:class (name path) :d (get icon-paths path) :key (name path)} path-props)])]))

(defn icon [icon-name & [{:keys [path-props svg-props]}]]
  [:i {:class (str "icon-" (name icon-name))}
   (svg-icon icon-name {:path-props path-props
                        :svg-props svg-props})])

(def spinner
  (icon :logomark-precursor))

(defn mixpanel-badge []
  [:a.mixpanel-badge {:href "https://mixpanel.com/f/partner"
                      :class "mobile-hidden"
                      :on-click #(.stopPropagation %)
                      :alt "Mobile Analytics"
                      :target "_blank"}
   [:svg {:width "114" :height "36"}
    [:path {:d "M39.2,13c0,1.1-0.8,2.1-2,2.1c-1.3,0-2-0.9-2-2.1c0-1.2,0.8-2.1,2-2.1C38.4,10.9,39.2,11.7,39.2,13z M72.5,15.4c1.2,0,2-0.9,2-2.2c0-1.2-0.8-2.2-2-2.2c-1.2,0-2,0.9-2,2.2C70.6,14.5,71.3,15.4,72.5,15.4z M76.1,24h2.2l-1.1-2.9 L76.1,24z M51.7,10.8c-1,0-1.7,0.7-1.8,1.6h3.5C53.4,11.5,52.7,10.8,51.7,10.8z M63.5,24h2.2l-1.1-2.9L63.5,24z M80.3,14.5 c0-0.5-0.3-0.8-0.9-0.8h-1.7v1.7h1.7C80,15.3,80.3,15,80.3,14.5z M30.1,10.8c-1.2,0-1.9,1-1.9,2.2c0,1.2,0.8,2.2,1.9,2.2 c1.2,0,1.9-0.9,1.9-2.2C32.1,11.7,31.3,10.8,30.1,10.8z M114,0v36H0V0H114z M84.7,16.2h3.7v-0.9h-2.6v-5.1h-1.1V16.2z M82.5,16.2 h1.1v-6h-1.1V16.2z M76.6,16.2h3c1.1,0,1.7-0.7,1.7-1.6c0-0.7-0.5-1.4-1.2-1.5c0.6-0.1,1-0.6,1-1.4c0-0.8-0.6-1.5-1.7-1.5h-3V16.2z M69.5,13.2c0,1.8,1.3,3.1,3.1,3.1c1.8,0,3.1-1.3,3.1-3.1c0-1.8-1.3-3.1-3.1-3.1C70.7,10.1,69.5,11.5,69.5,13.2z M62.2,16.2h1.1 v-4.6l1.8,4.6h0.5l1.8-4.6v4.6h1.1v-6H67l-1.6,4.1l-1.6-4.1h-1.5V16.2z M10.6,23.8c0-1.2-1-2.2-2.2-2.2s-2.2,1-2.2,2.2 c0,1.2,1,2.2,2.2,2.2S10.6,25.1,10.6,23.8z M13.7,10.9c1.1,0,1.5,0.8,1.5,1.8v2.9c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7v-3 c0-1.7-0.8-2.9-2.6-2.9c-1,0-1.8,0.4-2.4,1.3c-0.4-0.8-1.2-1.3-2.2-1.3c-0.9,0-1.6,0.4-2,1.2v-0.5c0-0.4-0.2-0.7-0.7-0.7 C6.3,9.8,6,10.1,6,10.5v5.1c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7v-2.9c0-1,0.6-1.8,1.7-1.8c1.1,0,1.6,0.8,1.6,1.8v2.9 c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7v-2.9C12,11.8,12.6,10.9,13.7,10.9z M17.9,23.9c0-0.9-0.7-1.6-1.6-1.6 c-0.9,0-1.6,0.7-1.6,1.6c0,0.9,0.7,1.6,1.6,1.6C17.2,25.4,17.9,24.7,17.9,23.9z M19.2,10.4c0-0.4-0.3-0.7-0.7-0.7 c-0.4,0-0.7,0.3-0.7,0.7v5.1c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7V10.4z M19.3,8c0-0.4-0.4-0.8-0.8-0.8 c-0.4,0-0.8,0.4-0.8,0.8c0,0.4,0.4,0.8,0.8,0.8C18.9,8.8,19.3,8.4,19.3,8z M23.6,23.9c0-0.4-0.3-0.7-0.7-0.7s-0.7,0.3-0.7,0.7 c0,0.4,0.3,0.7,0.7,0.7S23.6,24.3,23.6,23.9z M25.8,15.7c0-0.2-0.1-0.3-0.2-0.4L23.7,13l1.9-2.3c0.1-0.1,0.2-0.3,0.2-0.4 c0-0.3-0.2-0.6-0.6-0.6c-0.2,0-0.3,0.1-0.4,0.2L23,12.1L21.3,10c-0.1-0.1-0.3-0.2-0.4-0.2c-0.4,0-0.6,0.2-0.6,0.6 c0,0.1,0,0.3,0.2,0.4l1.9,2.3l-1.9,2.3c-0.1,0.1-0.2,0.2-0.2,0.4c0,0.3,0.3,0.6,0.6,0.6c0.2,0,0.4-0.1,0.5-0.2l1.7-2.1l1.7,2.1 c0.1,0.1,0.3,0.2,0.5,0.2C25.5,16.3,25.8,16,25.8,15.7z M33.4,13c0-1.9-1.4-3.3-3-3.3c-1,0-1.8,0.5-2.3,1.2v-0.5 c0-0.4-0.2-0.7-0.7-0.7s-0.6,0.3-0.6,0.7v7.8c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7v-3.2c0.5,0.7,1.3,1.2,2.2,1.2 C32.1,16.3,33.4,15,33.4,13z M40.6,13c0-1.9-1.2-3.3-3.3-3.3c-2,0-3.3,1.5-3.3,3.3c0,1.8,1.2,3.3,3.2,3.3c0.9,0,1.7-0.4,2.1-1.1v0.3 c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7h0V13z M47.7,12.7c0-1.7-1.3-3-3-3c-1.7,0-3,1.3-3,3v2.9c0,0.4,0.3,0.7,0.7,0.7 c0.4,0,0.7-0.3,0.7-0.7v-3c0-0.9,0.7-1.6,1.7-1.6c1,0,1.7,0.7,1.7,1.6v3c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7V12.7z M54.7,13c0-1.8-1.2-3.3-3-3.3c-1.9,0-3.2,1.4-3.2,3.3c0,1.8,1.1,3.3,3.3,3.3c1,0,2-0.4,2.6-1.1c0.1-0.1,0.2-0.2,0.2-0.4 c0-0.3-0.3-0.6-0.6-0.6c-0.2,0-0.3,0.1-0.4,0.2c-0.5,0.4-0.9,0.7-1.7,0.7c-1.2,0-1.9-0.8-1.9-1.7h4.3C54.6,13.5,54.7,13.3,54.7,13z M57,7.1c0-0.4-0.3-0.7-0.7-0.7c-0.4,0-0.7,0.3-0.7,0.7v8.5c0,0.4,0.3,0.7,0.7,0.7c0.4,0,0.7-0.3,0.7-0.7V7.1z M67.6,26.1l-2.4-6H64 l-2.4,6h1.2l0.4-1.2H66l0.4,1.2H67.6z M73.5,20.1h-1.1v4.2l-3.1-4.2h-1.1v6h1.1v-4.3l3.1,4.3h1V20.1z M80.2,26.1l-2.4-6h-1.3l-2.4,6 h1.2l0.4-1.2h2.8l0.4,1.2H80.2z M84.4,25.1h-2.6v-5.1h-1.1v6h3.7V25.1z M89.3,20.1h-1.2l-1.6,2.6l-1.6-2.6h-1.2l2.3,3.5v2.5H87v-2.5 L89.3,20.1z M89.3,16.2h4.1v-0.9h-3.1v-1.7h3v-0.9h-3v-1.5h3.1v-0.9h-4.1V16.2z M94.3,20.1h-4.7V21h1.8v5.1h1.1V21h1.8V20.1z M96.2,20.1h-1.1v6h1.1V20.1z M102.8,24.8l-0.9-0.5c-0.3,0.5-0.9,0.9-1.5,0.9c-1.2,0-2.1-0.9-2.1-2.2c0-1.3,0.9-2.2,2.1-2.2 c0.6,0,1.2,0.4,1.5,0.9l0.9-0.5c-0.4-0.7-1.2-1.3-2.4-1.3c-1.8,0-3.2,1.3-3.2,3.1c0,1.8,1.4,3.1,3.2,3.1 C101.6,26.2,102.3,25.5,102.8,24.8z M108,24.3c0-2.2-3.5-1.5-3.5-2.7c0-0.4,0.4-0.7,1-0.7c0.6,0,1.3,0.2,1.7,0.7l0.6-0.8 c-0.5-0.5-1.3-0.8-2.2-0.8c-1.3,0-2.2,0.8-2.2,1.8c0,2.2,3.5,1.4,3.5,2.7c0,0.4-0.3,0.8-1.2,0.8c-0.8,0-1.5-0.4-1.9-0.8l-0.6,0.8 c0.5,0.6,1.3,1,2.4,1C107.3,26.2,108,25.3,108,24.3z M80.2,11.9c0-0.4-0.3-0.8-0.8-0.8h-1.7v1.5h1.7C79.9,12.7,80.2,12.4,80.2,11.9z"}]]])

(defn google-login [& [size]]
  [:a.google-login {:class (when (= :small size) "small")
                    :role "button"}
   (icon :google)
   [:span {:data-first-half  "Sign in"
           :data-second-half "with Google"}]])

(defn updating-duration
  "Takes a :start time string and :stop time string. Updates the component every second
   if the stop-time is nil.
   By default, uses datetime/as-duration, but can also take a custom :formatter
   function in opts."
  [{:keys [start stop]} owner opts]
  (reify
    om/IDisplayName (display-name [_] "Updating Duration")
    om/IInitState
    (init-state [_]
      {:watcher-uuid (utils/uuid)
       :now (datetime/server-now)
       :has-watcher? false})
    om/IDidMount
    (did-mount [_]
      (when-not stop
        (let [timer-atom (om/get-shared owner [:timer-atom])
              uuid (om/get-state owner [:watcher-uuid])]
          (add-watch timer-atom uuid (fn [_k _r _p t]
                                       (om/set-state! owner [:now] t)))
          (om/set-state! owner [:has-watcher?] true))))
    om/IWillUnmount
    (will-unmount [_]
      (when (om/get-state owner [:has-watcher?])
        (remove-watch (om/get-shared owner [:timer-atom])
                      (om/get-state owner [:watcher-uuid]))))

    om/IDidUpdate
    (did-update [_ _ _]
      (when (and stop (om/get-state owner [:has-watcher?]))
        (remove-watch (om/get-shared owner [:timer-atom])
                      (om/get-state owner [:watcher-uuid]))))
    om/IRenderState
    (render-state [_ {:keys [now]}]
      (let [end-ms (if stop
                     (.getTime (js/Date. stop))
                     now)
            formatter (get opts :formatter datetime/as-duration)
            duration-ms (- end-ms (.getTime (js/Date. start)))]
        (dom/span nil (formatter duration-ms))))))
