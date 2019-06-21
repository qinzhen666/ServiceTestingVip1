package com.greenvalley.brainPlatform.brainPlatformApp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

class PatientManagerTest {

    PatientManager patientManager;
    Random random = new Random();
    @BeforeEach
    void setup(){
        if (patientManager == null){
            patientManager = new PatientManager();
        }
    }

    @Test
    void getPatientInfo() {
        //查询"patientName": "API测试患者","mobilephone": "13956588889",的患者
//        patientManager.createPatient().then().statusCode(200).body("status",equalTo("1"));
        patientManager.getPatientInfo("API测试患者","13956588889").then().statusCode(200)
                .body("body.patient.patientName",equalTo("API测试患者"))
                .body("body.patient.mobilephone",equalTo("13956588889"));
    }

    @Test
    void createPatient() {
        //创建"patientName": "API测试患者","mobilephone": "13956588889",的患者
        String mobilephone = "1397878";
        for (int i = 0;i < 4; i++){
            mobilephone+= random.nextInt(9);
        }
        patientManager.createPatient("API测试患者"+random.nextLong(),mobilephone)
                .then().statusCode(200).body("status",equalTo("1"));
    }


    @Test
    void deletePatient() {
        //删除患者，status改为0，非物理删除

        String patientName = "API测试患者"+random.nextLong();
        String mobilephone = "1397878";
        for (int i = 0;i < 4; i++){
            mobilephone+= random.nextInt(9);
        }
        Integer uid = patientManager.createPatient(patientName,mobilephone)
                .path("body.patient.uid");
        patientManager.deletePatient(uid).then().statusCode(200)
                .body("status",equalTo("1"));
        //检查是否删除
//        ArrayList<Object> nothing = new ArrayList<>();
        patientManager.getPatientInfoByList(patientName,0)
                .then().statusCode(200)
                .body("status",equalTo("1"))
                .body("body",equalTo(new ArrayList<>()));
    }

    @Test
    void getAllPatientInfoByList() {
        patientManager.getAllPatientInfoByList().then().statusCode(200).body("status",equalTo("1"));
    }

    @Test
    void getPatientInfoByList() {
//        根据列表patientName和medicalHistory(否，0)条件查询已创建的患者
        String mobilephone = "1397878";
        for (int i = 0;i < 4; i++){
            mobilephone+= random.nextInt(9);
        }
        String patientName = patientManager.createPatient("API测试患者"+random.nextLong(),mobilephone)
                                            .path("body.patient.patientName").toString();
        System.out.println(patientName);
        patientManager.getPatientInfoByList(patientName,0)
                      .then().statusCode(200)
                      .body("status",equalTo("1"))
                      .body("body.medicalHistoryType[0].medicalHistoryIdentify[0]",equalTo(0))
                      .body("body.patient.patientName[0]",equalTo(patientName));
    }


    @Test
    void updatePatientNoChange() {
        //1、选择“编辑患者”address，若患者"patientName"和"mobilephone"都不变，则为编辑,"status"传入1
        patientManager.updatePatient("address","上海市上海徐汇区南站",1)
                .then().statusCode(200)
                .body("status",equalTo("1"));
        patientManager.getPatientInfo("酷奇包包","15555555555")
                .then().statusCode(200)
                .body("body.patient.patientName",equalTo("酷奇包包"))
                .body("body.patient.mobilephone",equalTo("15555555555"))
                .body("body.patient.address",equalTo("上海市上海徐汇区南站"));
    }

    @Test
    void updatePatientChangeName(){
        //2、选择“编辑患者”patientName，若患者"patientName"和"mobilephone"都任意一个发生改变，则为编新增,"status"传入1
        String updateName = "酷奇包包"+random.nextLong();
        patientManager.updatePatient("patientName",updateName,1)
                .then().statusCode(200)
                .body("status",equalTo("1"));
        //检查是否新增了一名同mobilephone，不同patientName的患者
        patientManager.getPatientInfoByList(updateName,0)
                .then().statusCode(200)
                .body("status",equalTo("1"))
                .body("body.patient.mobilephone[0]",equalTo("15555555555"))
                .body("body.patient.patientName[0]",equalTo(updateName));
    }

    @Test
    void createExistingPatient(){
        //3、选择“创建患者”，若检查患者"patientName"和"mobilephone"在数据库已存在，则为编辑，"status"传入0
        //已存在患者更新address，传入status为0
        patientManager.updatePatient("address","杭州西湖区",0)
                .then().statusCode(200)
                .body("body.patient.patientName",equalTo("酷奇包包"))
                .body("body.patient.mobilephone",equalTo("15555555555"))
                .body("body.patient.address",equalTo("杭州西湖区"));
    }
}