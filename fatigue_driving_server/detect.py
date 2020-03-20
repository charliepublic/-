import cv2
import dlib
import imutils
from imutils import face_utils
from scipy.spatial import distance


class eyes_detect():
    def __init__(self):

        self.thresh = 0.25
        self.frame_check = 10
        self.detect = dlib.get_frontal_face_detector()
        self.predict = dlib.shape_predictor("shape_predictor_68_face_landmarks.dat")  # Dat file is the crux of the code
        self.flag = 0
        (self.lStart, self.lEnd) = face_utils.FACIAL_LANDMARKS_68_IDXS["left_eye"]
        (self.rStart, self.rEnd) = face_utils.FACIAL_LANDMARKS_68_IDXS["right_eye"]

    def eye_aspect_ratio(self, eyes):
        A = distance.euclidean(eyes[1], eyes[5])
        B = distance.euclidean(eyes[2], eyes[4])
        C = distance.euclidean(eyes[0], eyes[3])
        eye_distance = (A + B) / (2.0 * C)
        return eye_distance

    def detect_picture(self, img):
        print("get")
        frame = imutils.resize(img, width=450, height= 900)
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        subjects = self.detect(gray, 0)
        for subject in subjects:
            shape = self.predict(gray, subject)
            shape = face_utils.shape_to_np(shape)  # converting to NumPy Array
            leftEye = shape[self.lStart:self.lEnd]
            rightEye = shape[self.rStart:self.rEnd]
            leftEAR = self.eye_aspect_ratio(leftEye)
            rightEAR = self.eye_aspect_ratio(rightEye)
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
        cv2.imshow("Frame", frame)
        cv2.waitKey(1)
