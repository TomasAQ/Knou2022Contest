import os
import easyocr
from flask import Flask, request , render_template
from werkzeug.utils import secure_filename

from Private_serverInfo import requestForm , server_host , server_port , distinction_String
from PIL import Image

app = Flask(__name__)
reader = ""

@app.route('/')
def index():
    global reader
    img_path = '01.jpg'
    result = reader.readtext(img_path)
    return ''+str(result)

@app.route('/fileUpload',methods=['GET','POST'])
def upload_file():

    if request.method == 'POST':
        f = request.files[requestForm]
        filenames = secure_filename(f.filename)
        print("이미지 저장")
        f.save("./upImg/"+filenames+".jpg")
        print("이미지 저장 Sucess")
        result = extractText(filenames)
        print(result)

    return result

def extractText(filename):
    global reader
    extraData = ""
    # image = Image.open('./upImg/'+filename).convert('RGB')
    print("이미지 분석")
    result = reader.readtext("./upImg/"+filename+".jpg")
    print("이미지 분석 Sucess")

    for bbox , text , conf in result :
        extraData = extraData + (distinction_String + text)
        # if conf > 0.5 :
        #     extraData = distinction_String + text
    return extraData


if __name__ == '__main__':
    reader = easyocr.Reader(['ko','en'],gpu=False)
    print("모델 로드 완료")
    app.run(host = server_host , port=server_port)
