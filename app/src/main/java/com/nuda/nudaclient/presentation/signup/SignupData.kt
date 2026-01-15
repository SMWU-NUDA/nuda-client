package com.nuda.nudaclient.presentation.signup

// 회원가입 데이터를 임시 저장할 싱글톤
object SignupData {
    // 1단계 기본정보
    var nickname : String = ""
    var username : String = "" // 아이디
    var password : String = ""
    var email : String = ""

    // 2단계 배송정보
    var recipient : String = ""
    var phoneNum : String = "" // 하이픈 형태로 요청
    var postalCode : String = "" // 우편번호
    var address1 : String = "" // 주소
    var address2 : String = "" // 상세 주소

    // 3단계 설문
    var irritationLevel : String = "" // 자극 정도 (민감도)
    var scent : String = "" // 향
    var changeFrequency : String = "" // 교체 정도(양)
    var thickness : String = "" // 두께
    var priority : String = "" // 우선순위

    // 모든 데이터 초기화
    fun clear() {
        var nickname : String = ""
        var username : String = ""
        var password : String = ""
        var email : String = ""

        var recipient : String = ""
        var phoneNum : String = ""
        var postalCode : String = ""
        var address1 : String = ""
        var address2 : String = ""

        var irritationLevel : String = ""
        var scent : String = "" // 향
        var changeFrequency : String = ""
        var thickness : String = ""
        var priority : String = ""
    }
}