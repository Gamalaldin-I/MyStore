package com.example.htopstore.util.firebase

object FirebaseUtils {
    //FOR STORES
    const val STORES = "stores"
    const val STORE_ID= "HtotpStore1"
    const val STORE_NAME = "name"
    const val STORE_LOCATION = "location"
    const val STORE_PHONE = "phone"
    const val STORE_OWNER_ID = "ownerId"


    //FOR AUTH

    //FOR OWNERS
    const val OWNERS = "owners"
    const val OWNER_NAME = "name"
    const val OWNER_EMAIL = "email"
    const val OWNER_PASSWORD = "password"
    const val OWNER_ID = "ownerID"
    const val OWNER_STORE_ID = "storeID"
    const val OWNER_ROLE = "role"
    const val OWNER_CREATED_AT = "createdAt"

    //PENDING REQUESTS
    const val PENDING_REQUESTS = "pendingRequests"
    const val REQUEST_STATUS = "status"
    const val EMPLOYEE_ID = "employeeId"
    const val REQUEST_NAME = "name"
    const val REQUEST_PASSWORD = "password"
    const val REQUEST_EMAIL = "email"
    const val REQUEST_CREATED_AT = "requestedAt"
    const val REQUEST_CODE = "code"



    //FOR EMPLOYEES
    const val EMPLOYEES = "employees"
    const val EMPLOYEE_NAME = "name"
    const val EMPLOYEE_EMAIL = "email"
    const val EMPLOYEE_PASSWORD = "password"
    const val EMPLOYEE_ROLE = "role"

    //for invites
    const val INVITES = "invites"
    const val INVITE_EMAIL = "email"
    const val INVITE_STATUS = "status"
    const val INVITE_CREATED_AT = "createAt"
    const val INVITE_ACCEPTED_AT = "acceptedAt"
    const val INVITE_ID = "id"



    //FOR PRODUCT
    const val PRODUCTS = "products"
    const val PRODUCT_NAME ="name"
    const val PRODUCT_BUYING_PRICE ="buyingPrice"
    const val PRODUCT_COUNT ="count"
    const val PRODUCT_ADDING_DATE="addingDate"
    const val PRODUCT_CATEGORY = "category"
    const val PRODUCT_IMAGE_URL = "imageURL"
    const val PRODUCT_SELLING_PRICE ="sellingPrice"
    const val PRODUCT_SOLD_COUNT = "soldCount"


    const val EXPENSES="expenses"
    const val BILLS ="bills"
    const val SOLD_PRODUCTS="soldProducts"

}