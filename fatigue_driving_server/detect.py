import cv2
import imutils
from imutils import face_utils
from scipy.spatial import distance

from gaze_tracking import GazeTracking


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
        self.flag = 0
        self.gaze = GazeTracking()
        (self.lStart, self.lEnd) = face_utils.FACIAL_LANDMARKS_68_IDXS["left_eye"]
        (self.rStart, self.rEnd) = face_utils.FACIAL_LANDMARKS_68_IDXS["right_eye"]

    def detect_picture(self, frame):
        # print("get")

        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        subjects = self.gaze.face_detector(gray, 0)
        for subject in subjects:
            shape = self.gaze.predictor(gray, subject)
            shape = face_utils.shape_to_np(shape)  # converting to NumPy Array
            leftEye = shape[self.lStart:self.lEnd]
            rightEye = shape[self.rStart:self.rEnd]
            leftEAR = eye_aspect_ratio(leftEye)
            rightEAR = eye_aspect_ratio(rightEye)
            ear = (leftEAR + rightEAR) / 2.0
            leftEyeHull = cv2.convexHull(leftEye)
            rightEyeHull = cv2.convexHull(rightEye)
            cv2.drawContours(frame, [leftEyeHull], -1, (0, 255, 0), 1)
            cv2.drawContours(frame, [rightEyeHull], -1, (0, 255, 0), 1)
            if ear < self.thresh:
                self.flag += 1
                print(self.flag)
                if self.flag >= self.frame_check:
                    print("sleepy")
                    cv2.putText(frame, "****************ALERT!****************", (10, 30),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
                    cv2.putText(frame, "****************ALERT!****************", (10, 325),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
            else:
                self.flag = 0

        self.gaze.refresh(frame)

        frame = self.gaze.annotated_frame()
        text = ""

        if self.gaze.is_blinking():
            text = "Blinking"
        elif self.gaze.is_right():
            text = "Looking right"
        elif self.gaze.is_left():
            text = "Looking left"
        elif self.gaze.is_center():
            text = "Looking center"

        cv2.putText(frame, text, (90, 60), cv2.FONT_HERSHEY_DUPLEX, 1.6, (147, 58, 31), 2)

        left_pupil = self.gaze.pupil_left_coords()
        right_pupil = self.gaze.pupil_right_coords()
        cv2.putText(frame, "Left pupil:  " + str(left_pupil), (90, 130), cv2.FONT_HERSHEY_DUPLEX, 0.9, (147, 58, 31), 1)
        cv2.putText(frame, "Right pupil: " + str(right_pupil), (90, 165), cv2.FONT_HERSHEY_DUPLEX, 0.9, (147, 58, 31),
                    1)

        cv2.imshow("Frame", frame)
        cv2.waitKey(1)
