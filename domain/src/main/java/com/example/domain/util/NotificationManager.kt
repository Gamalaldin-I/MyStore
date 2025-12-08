package com.example.domain.util

import com.example.domain.model.Bill
import com.example.domain.model.Notification
import com.example.domain.model.Product
import com.example.domain.model.SoldProduct
import com.example.domain.model.User
import java.util.Locale

/**
 * Manager for creating and managing notifications across the application.
 * Supports bilingual notifications (Arabic and English) based on device locale.
 */
object NotificationManager {

    // Notification Types
    enum class NotificationType(val value: String) {
        DELETE("deleted"),
        ADD("added"),
        UPDATE("updated"),
        SELL("sell"),
        EXPENSE("expense"),
        STORE("store"),
        BILL("bill"),
        USER("user");

        companion object {
            fun fromValue(value: String): NotificationType? = entries.find { it.value == value }
        }
    }

    // Notification Colors
    private enum class NotificationColor(val hex: String) {
        GREEN("#4CAF50"),
        RED("#F44336"),
        ORANGE("#FF9800"),
        BLUE("#2196F3"),
        PURPLE("#9C27B0"),
        CYAN("#00BCD4"),
        AMBER("#FFC107"),
        BLUE_GREY("#607D8B"),
        GREY("#757575")
    }

    // Cached locale check
    private val isArabic: Boolean
        get() = Locale.getDefault().language == "ar"

    // ============================================
    // Product Notifications
    // ============================================

    fun createAddProductNotification(user: User, storeId: String, product: Product): Notification =
        createNotification(
            user = user,
            storeId = storeId,
            productId = product.id,
            productName = product.name,
            type = NotificationType.ADD,
            arabicMsg = "قام ${user.name} بإضافة منتج جديد: ${product.name}",
            englishMsg = "${user.name} added a new product: ${product.name}"
        )

    fun createDeleteProductNotification(user: User, storeId: String, productId: String): Notification =
        createNotification(
            user = user,
            storeId = storeId,
            productId = productId,
            type = NotificationType.DELETE,
            arabicMsg = "قام ${user.name} بحذف منتج: $productId",
            englishMsg = "${user.name} deleted a product: $productId"
        )

    fun createUpdateProductNotification(user: User, storeId: String, product: Product): Notification =
        createNotification(
            user = user,
            storeId = storeId,
            productId = product.id,
            productName = product.name,
            type = NotificationType.UPDATE,
            arabicMsg = "قام ${user.name} بتحديث منتج: ${product.name}",
            englishMsg = "${user.name} updated a product: ${product.name}"
        )

    // ============================================
    // User Notifications
    // ============================================

    fun createAddUserNotification(user: User, storeId: String, newUserName: String): Notification =
        createNotification(
            user = user,
            storeId = storeId,
            type = NotificationType.USER,
            arabicMsg = "قام ${user.name} بإضافة مستخدم جديد: $newUserName",
            englishMsg = "${user.name} added a new user: $newUserName"
        )

    fun createFireUserNotification(user: User, storeId: String, deletedUserName: String, rejected: Boolean): Notification =
        createNotification(
            user = user,
            storeId = storeId,
            type = if (rejected) NotificationType.DELETE else NotificationType.UPDATE,
            arabicMsg = if (rejected) "قام ${user.name} بإيقاف مستخدم: $deletedUserName"
            else "قام ${user.name} بتفعيل مستخدم: $deletedUserName",
            englishMsg = if (rejected) "${user.name} removed a user: $deletedUserName"
            else "${user.name} activated a user: $deletedUserName"
        )
    fun createUpdateUserNotification(user: User, storeId: String, updatedUserName: String): Notification =
        createNotification(
            user = user,
            storeId = storeId,
            type = NotificationType.UPDATE,
            arabicMsg = "قام ${user.name} بتحديث معلومات المستخدم: $updatedUserName",
            englishMsg = "${user.name} updated user information: $updatedUserName"
        )

    // ============================================
    // Store Notifications
    // ============================================

    fun createUpdateStoreNotification(user: User, storeId: String, storeName: String): Notification =
        createNotification(
            user = user,
            storeId = storeId,
            type = NotificationType.UPDATE,
            arabicMsg = "قام ${user.name} بتحديث معلومات المتجر: $storeName",
            englishMsg = "${user.name} updated store information: $storeName"
        )

    // ============================================
    // Sell/Bill Notifications
    // ============================================

    fun createSellProductNotification(user: User, storeId: String, bill: Bill, totalPrice: Double): Notification =
        createNotification(
            user = user,
            storeId = storeId,
            billId = bill.id,
            type = NotificationType.SELL,
            arabicMsg = "تم البيع بمبلغ $totalPrice من ${bill.totalCash}",
            englishMsg = "Sale completed for $totalPrice from ${bill.totalCash}"
        )

    fun createUpdateBillNotification(user: User, storeId: String, billId: String, returnProduct: SoldProduct): Notification {
        val returnAmount = returnProduct.sellingPrice * returnProduct.quantity
        return createNotification(
            user = user,
            storeId = storeId,
            billId = billId,
            type = NotificationType.BILL,
            arabicMsg = "قام ${user.name} بتحديث فاتورة: إرجاع ${returnProduct.quantity} ${returnProduct.name} بمبلغ $returnAmount",
            englishMsg = "${user.name} updated bill: returned ${returnProduct.quantity} ${returnProduct.name} for $returnAmount"
        )
    }

    fun createDeleteBillNotification(user: User, storeId: String, billId: String): Notification =
        createNotification(
            user = user,
            storeId = storeId,
            billId = billId,
            type = NotificationType.DELETE,
            arabicMsg = "قام ${user.name} بحذف فاتورة",
            englishMsg = "${user.name} deleted a bill"
        )

    // ============================================
    // Expense Notifications
    // ============================================

    fun createAddExpenseNotification(user: User, storeId: String, expenseName: String, amount: Double): Notification =
        createNotification(
            user = user,
            storeId = storeId,
            type = NotificationType.EXPENSE,
            arabicMsg = "قام ${user.name} بإضافة مصروف: $expenseName بمبلغ $amount",
            englishMsg = "${user.name} added an expense: $expenseName with amount $amount"
        )

    fun createDeleteExpenseNotification(user: User, storeId: String, expenseName: String): Notification =
        createNotification(
            user = user,
            storeId = storeId,
            type = NotificationType.DELETE,
            arabicMsg = "قام ${user.name} بحذف مصروف: $expenseName",
            englishMsg = "${user.name} deleted an expense: $expenseName"
        )

    // ============================================
    // Helper Methods
    // ============================================

    private fun createNotification(
        user: User,
        storeId: String,
        productId: String = "",
        productName: String = "",
        billId: String = "",
        type: NotificationType,
        arabicMsg: String,
        englishMsg: String
    ): Notification = Notification(
        id = IdGenerator.generateTimestampedId(),
        createdAt = DateHelper.getCurrentTimestampTz(),
        deleted = false,
        userId = user.id,
        userImage = user.photoUrl,
        storeId = storeId,
        productId = productId,
        productName = productName,
        billId = billId,
        type = type.value,
        description = if (isArabic) arabicMsg else englishMsg
    )

    // ============================================
    // UI Helper Methods
    // ============================================

    fun getNotificationTypeLabel(type: String): String {
        return when (NotificationType.fromValue(type)) {
            NotificationType.ADD -> if (isArabic) "إضافة" else "Added"
            NotificationType.DELETE -> if (isArabic) "حذف" else "Deleted"
            NotificationType.UPDATE -> if (isArabic) "تحديث" else "Updated"
            NotificationType.SELL -> if (isArabic) "بيع" else "Sale"
            NotificationType.EXPENSE -> if (isArabic) "مصروف" else "Expense"
            NotificationType.STORE -> if (isArabic) "متجر" else "Store"
            NotificationType.BILL -> if (isArabic) "فاتورة" else "Bill"
            NotificationType.USER -> if (isArabic) "مستخدم" else "User"
            null -> type
        }
    }

    fun getNotificationIcon(type: String): Int {
        return when (NotificationType.fromValue(type)) {
            NotificationType.ADD -> android.R.drawable.ic_input_add
            NotificationType.DELETE -> android.R.drawable.ic_menu_delete
            NotificationType.UPDATE -> android.R.drawable.ic_menu_edit
            NotificationType.SELL -> android.R.drawable.ic_menu_send
            NotificationType.EXPENSE -> android.R.drawable.ic_menu_report_image
            NotificationType.STORE -> android.R.drawable.ic_menu_info_details
            NotificationType.BILL -> android.R.drawable.ic_menu_agenda
            NotificationType.USER -> android.R.drawable.ic_menu_my_calendar
            null -> android.R.drawable.ic_dialog_info
        }
    }

    fun getNotificationColor(type: String): String {
        val color = when (NotificationType.fromValue(type)) {
            NotificationType.ADD -> NotificationColor.GREEN
            NotificationType.DELETE -> NotificationColor.RED
            NotificationType.UPDATE -> NotificationColor.ORANGE
            NotificationType.SELL -> NotificationColor.BLUE
            NotificationType.EXPENSE -> NotificationColor.PURPLE
            NotificationType.STORE -> NotificationColor.CYAN
            NotificationType.BILL -> NotificationColor.AMBER
            NotificationType.USER -> NotificationColor.BLUE_GREY
            null -> NotificationColor.GREY
        }
        return color.hex
    }
}