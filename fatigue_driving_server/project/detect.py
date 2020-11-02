import time

import cv2
from imutils import face_utils
from numba import jit
from scipy.spatial import distance

from project import database as db
from project.gaze_tracking import GazeTracking


def eye_aspect_ratio(eyes):
    A = distance.euclidean(eyes[1], eyes[5])
    B = distance.euclidean(eyes[2], eyes[4])
    C = distance.euclidean(eyes[0], eyes[3])
    eye_distance = (A + B) / (2.0 * C)
    return eye_distance


class eyes_detect():
    def __init__(self):

        self.thresh = 0.25
        self.frame_check = 10
        self.isTired = 0
        self.gaze = GazeTracking()
        (self.lStart, self.lEnd) = face_utils.FACIAL_LANDMARKS_68_IDXS["left_eye"]
        (self.rStart, self.rEnd) = face_utils.FACIAL_LANDMARKS_68_IDXS["right_eye"]
        print("detect init")

    @jit(forceobj=True)
    def onload(self, frame):
        cv2.imshow("detect", frame)
        cv2.waitKey(1)
        # Start time
        start = time.time()
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        subjects = self.gaze.face_detector(gray, 0)
        shape = self.gaze.predictor(gray, subjects[0])
        shape = face_utils.shape_to_np(shape)
        leftEye = shape[self.lStart:self.lEnd]
        rightEye = shape[self.rStart:self.rEnd]

        leftEAR = eye_aspect_ratio(leftEye)
        rightEAR = eye_aspect_ratio(rightEye)
        ear = (leftEAR + rightEAR) / 2.0

        leftEyeHull = cv2.convexHull(leftEye)
        rightEyeHull = cv2.convexHull(rightEye)
        cv2.drawContours(frame, [leftEyeHull], -1, (0, 255, 0), 1)
        cv2.drawContours(frame, [rightEyeHull], -1, (0, 255, 0), 1)
        # End time
        end = time.time()
        # Time elapsed
        seconds = end - start
        # fps = 1 / seconds
        print("onload : {0}".format(seconds))
        return ear

    def detect_picture(self, frame, notify, phone):
        # print("get")

        # Start time
        start = time.time()

        phone_number = phone["phone"]
        # print("detect get phoneNumber:" + phone_number + "\n")

        database = db.database_operation()
        database.refresh(phone_number)
        try:
            ear = self.onload(frame)
            if ear < self.thresh:
                self.isTired += 1
                print("isTired 目前为")
                print(self.isTired)
                if self.isTired >= self.frame_check:
                    print("sleepy")
                    database.update(phone_number, database.alert)
                    notify.put(True)
                else:
                    notify.put(False)
            else:
                self.isTired = 0
                notify.put(False)
        except IndexError:
            print("detect err")
            notify.put(False)

        # End time
        end = time.time()
        # Time elapsed
        seconds = end - start
        # fps = 1 / seconds
        print("first : {0}".format(seconds))

        # Start time
        start = time.time()
        self.gaze.refresh(frame)

        frame = self.gaze.annotated_frame()
        text = ""

        if self.gaze.is_right():
            text = "Looking right"
            print(text)
            database.update(phone_number, database.right)
        elif self.gaze.is_left():
            text = "Looking left"
            print(text)
            database.update(phone_number, database.left)
        elif self.gaze.is_center():
            text = "Looking center"
            print(text)
            database.update(phone_number, database.center)
        database.close_database()
        # cv2.putText(frame, text, (90, 60), cv2.FONT_HERSHEY_DUPLEX, 1.6, (147, 58, 31), 2)
        # End time
        end = time.time()
        # Time elapsed
        seconds = end - start
        # fps = 1 / seconds
        # print("second : {0}".format(seconds))

        cv2.imshow("Frame", frame)
        cv2.waitKey(1)
