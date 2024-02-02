package com.kemarport.mahindrakiosk.helper

object Constants {


    const val LOGGEDIN = "isLoggedIn"
    const val IS_ADMIN = "isAdmin"
    const val USERNAME = "username"
    const val TOKEN = "token"

    const val NO_INTERNET = "No Internet Connection"
    const val NETWORK_FAILURE = "Network Failure"
    const val CONFIG_ERROR = "Please configure network details"
    const val INCOMPLETE_DETAILS = "Please fill the required details"
    const val EXCEPTION_ERROR="No Data Found"

    const val SHARED_PREF = "shared_pref"
    const val SERVER_IP = "server_ip"
    const val ISFIRSTTIME = "is_first_time"

    const val SERVER_IP_SHARED = "192.168.1.105"

    const val GET = 1
    const val POST = 2
    const val HTTP_OK = 200
    const val HTTP_CREATED = 201
    const val HTTP_EXCEPTION = 202
    const val HTTP_UPDATED = 204
    const val HTTP_FOUND = 302
    const val HTTP_NOT_FOUND = 404
    const val HTTP_CONFLICT = 409
    const val HTTP_INTERNAL_SERVER_ERROR = 500

    const val ADD_LOCATIONS = "LocationMapping/AddLocations"
    const val GET_PARENT_LOCATION = "LocationMapping/GetParentLocation"
   // const val LOGIN_URL = "UserManagement/authenticate"
    const val VIN_VALIDATE = "Vehicle/GetVehicleInfo"
    const val KEMAR_GET_VEHICLE_INFORMATION = "DMSRestSIT/rest/Kemar/GetVehicleInformation"
    const val KEMAR_VEHICLE_CONFIRMATION = "DMSRestSIT/rest/Kemar/UpdateDeliveryConfirmation"
    const val DMS_BASE_URL = " https://mdwaccess.com/"
    //const val ADD_VEHICLE_RFID_MAPPING = "VehicleRFID/AddVehicleRFIDMapping"
    const val GET_VEHICLE_RFID_MAPPING = "VehicleRFID/GetVehicleRFIDMapping"

    const val VEHICLE_GET_VEHICLE_INFO = "VehicleRFID/GetVehicleRFIDMapping"
    const val VEHICLE_CONFIRMATION = "DealerVehicle/SaveDealerVehicle"
    const val VEHICLE_CONFIRMATION_DASH = "DealerVehicle/GetVehicleConfirmation"

    //const val BASE_URL_LOCAL = "http://192.168.1.34:5000/api/"
    //const val BASE_URL_LOCAL = "http://103.240.90.141:5050/Service/api/"
    //const val BASE_URL_LOCAL = "http://192.168.1.20:5000/api/"
    //const val BASE_URL_LOCAL = "http://192.168.1.205:8011/Service/api/"

    //const val BASE_URL_LOCAL = "http://rfid-yard-lb-1652367993.ap-south-1.elb.amazonaws.com:82/api/"

    const val BASE_URL_LOCAL = "http://103.240.90.141:5050/service/api/"
    //const val BASE_URL_LOCAL = "http://103.240.90.141:5050/Service/api/"

    //merging
    const val VEHICLE_CONFIRMATION_COUNT = "DealerVehicle/GetVehicleConfirmationCountMobile"
    const val VEHICLE_RFID_COUNT = "DealerVehicle/GetScanRFIDCount"
    //const val BASE_URL = "http://rfid-yard-lb-1652367993.ap-south-1.elb.amazonaws.com:82/api/"

    const val BASE_URL = "http://103.240.90.141:5050/service/api/"
    //const val BASE_URL = "http://192.168.1.32:5000/api/"
    //const val BASE_URL = "http://192.168.1.5:5000/api/"



    //new apis from Girish Sir
    const val HTTP_ERROR_MESSAGE = "message"
    const val LOCATION_ID="locationId"
    const val KEY_USER_NAME = "userName"
    const val DEALER_CODE = "dealerCode"
    const val KEY_USER_FIRST_NAME = "firstName"
    const val KEY_USER_LAST_NAME= "lastName"
    const val KEY_USER_EMAIL = "email"
    const val KEY_USER_MOBILE_NUMBER = "mobileNumber"
    const val KEY_USER_IS_VERIFIED = "isVerified"
    const val COORDINATES = "coordinates"
    const val KEY_JWT_TOKEN = "jwtToken"
    const val ROLE_NAME = "roleName"
    const val KEY_REFRESH_TOKEN = "refreshToken"
    const val KEY_ISLOGGEDIN = "isLoggedIn"




    const val LOGIN_URL = "UserManagement/authenticate"
    const val VERIFY_DEALER_VEHICLE = "MobileService/verifyDealerVehicle"
    const val CONFIRM_DEALER_VEHICLE_DELIVERY = "MobileService/confirmDealerVehicleDelivery"
    const val GET_DELIVERED_COUNT = "Dashboard/getDeliveredCount"
    const val GET_DELIVERED_DETAILS = "Dashboard/getDeliveredDetails"
    const val  CHANGE_PASSWORD= "UserManagement/change-password"
    const val  FORGOT_PASSWORD= "UserManagement/forgot-password"
    const val  RESET_PASSWORD= "UserManagement/reset-password"

}