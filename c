[1mdiff --git a/app/src/main/java/com/example/myapplication/models/Child.java b/app/src/main/java/com/example/myapplication/models/Child.java[m
[1mindex 8434559..f0e8b2d 100644[m
[1m--- a/app/src/main/java/com/example/myapplication/models/Child.java[m
[1m+++ b/app/src/main/java/com/example/myapplication/models/Child.java[m
[36m@@ -113,8 +113,8 @@[m [mpublic class Child extends User{[m
     // For reference, LocalDate.of(int year, int month, int day) may be used for changing if needed[m
 [m
     // When use medicine, automatically add to streak[m
[31m-    public void useMedicine(int index, double amount, LocalDateTime timestamp, TechniqueQuality techniqueQuality) {[m
[31m-        inventory.useMedicine(index, amount, timestamp, techniqueQuality);[m
[32m+[m[32m    public void useMedicine(int index, double amount, LocalDateTime timestamp) {[m
[32m+[m[32m        inventory.useMedicine(index, amount, timestamp);[m
         streakCount.countStreaks();[m
         badges.updateControllerBadge();[m
         badges.updateTechniqueBadge();[m
[1mdiff --git a/app/src/main/res/layout/activity_inventory_edit.xml b/app/src/main/res/layout/activity_inventory_edit.xml[m
[1mnew file mode 100644[m
[1mindex 0000000..c4e0ec2[m
[1m--- /dev/null[m
[1m+++ b/app/src/main/res/layout/activity_inventory_edit.xml[m
[36m@@ -0,0 +1,200 @@[m
[32m+[m[32m<?xml version="1.0" encoding="utf-8"?>[m
[32m+[m[32m<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"[m
[32m+[m[32m    xmlns:app="http://schemas.android.com/apk/res-auto"[m
[32m+[m[32m    xmlns:tools="http://schemas.android.com/tools"[m
[32m+[m[32m    android:layout_width="match_parent"[m
[32m+[m[32m    android:layout_height="match_parent"[m
[32m+[m[32m    android:background="#E8F5E9">[m
[32m+[m
[32m+[m[32m    <!-- Top Bar -->[m
[32m+[m[32m    <LinearLayout[m
[32m+[m[32m        android:id="@+id/topBar"[m
[32m+[m[32m        android:layout_width="match_parent"[m
[32m+[m[32m        android:layout_height="wrap_content"[m
[32m+[m[32m        android:orientation="horizontal"[m
[32m+[m[32m        android:background="#064200"        android:padding="16dp"[m
[32m+[m[32m        android:gravity="center_vertical"[m
[32m+[m[32m        app:layout_constraintTop_toTopOf="parent">[m
[32m+[m
[32m+[m[32m        <ImageButton[m
[32m+[m[32m            android:id="@+id/btnBack"[m
[32m+[m[32m            android:layout_width="48dp"[m
[32m+[m[32m            android:layout_height="48dp"[m
[32m+[m[32m            android:src="@android:drawable/ic_menu_revert"[m
[32m+[m[32m            android:background="?attr/selectableItemBackgroundBorderless"[m
[32m+[m[32m            android:contentDescription="Back"[m
[32m+[m[32m            tools:tint="@color/white"/>[m
[32m+[m
[32m+[m[32m        <TextView[m
[32m+[m[32m            android:layout_width="0dp"[m
[32m+[m[32m            android:layout_height="wrap_content"[m
[32m+[m[32m            android:layout_weight="1"[m
[32m+[m[32m            android:text="Edit Medicine"[m
[32m+[m[32m            android:textSize="22sp"[m
[32m+[m[32m            android:textStyle="bold"[m
[32m+[m[32m            android:textColor="@color/white"[m
[32m+[m[32m            android:gravity="center"/>[m
[32m+[m
[32m+[m[32m        <Button[m
[32m+[m[32m            android:id="@+id/btnSaveMedicine"[m
[32m+[m[32m            android:layout_width="wrap_content"[m
[32m+[m[32m            android:layout_height="wrap_content"[m
[32m+[m[32m            android:text="Save"[m
[32m+[m[32m            android:backgroundTint="@color/white"[m
[32m+[m[32m            android:textColor="#064200"/>[m
[32m+[m
[32m+[m[32m    </LinearLayout>[m
[32m+[m
[32m+[m[32m    <ScrollView[m
[32m+[m[32m        android:layout_width="match_parent"[m
[32m+[m[32m        android:layout_height="0dp"[m
[32m+[m[32m        app:layout_constraintTop_toBottomOf="@id/topBar"[m
[32m+[m[32m        app:layout_constraintBottom_toBottomOf="parent">[m
[32m+[m
[32m+[m[32m        <LinearLayout[m
[32m+[m[32m            android:layout_width="match_parent"[m
[32m+[m[32m            android:layout_height="wrap_content"[m
[32m+[m[32m            android:orientation="vertical"[m
[32m+[m[32m            android:padding="24dp">[m
[32m+[m
[32m+[m[32m            <!-- All fields identical to create layout -->[m
[32m+[m[32m            <!-- (medicine name → purchase date → expiry → capacity → remaining → label dropdown) -->[m
[32m+[m
[32m+[m[32m            <!-- Medicine Name -->[m
[32m+[m[32m            <com.google.android.material.textfield.TextInputLayout[m
[32m+[m[32m                android:layout_width="match_parent"[m
[32m+[m[32m                android:layout_height="wrap_content"[m
[32m+[m[32m                android:layout_marginBottom="16dp"[m
[32m+[m[32m                android:hint="Medicine Name"[m
[32m+[m[32m                app:boxCornerRadiusTopStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusTopEnd="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomEnd="12dp"[m
[32m+[m[32m                app:boxBackgroundColor="@color/white">[m
[32m+[m
[32m+[m[32m                <com.google.android.material.textfield.TextInputEditText[m
[32m+[m[32m                    android:id="@+id/etMedicineName"[m
[32m+[m[32m                    android:layout_width="match_parent"[m
[32m+[m[32m                    android:layout_height="wrap_content"[m
[32m+[m[32m                    android:inputType="text"/>[m
[32m+[m
[32m+[m[32m            </com.google.android.material.textfield.TextInputLayout>[m
[32m+[m
[32m+[m[32m            <!-- Purchase Date -->[m
[32m+[m[32m            <com.google.android.material.textfield.TextInputLayout[m
[32m+[m[32m                android:layout_width="match_parent"[m
[32m+[m[32m                android:layout_height="wrap_content"[m
[32m+[m[32m                android:layout_marginBottom="16dp"[m
[32m+[m[32m                android:hint="Purchase Date (MM/DD/YYYY)"[m
[32m+[m[32m                app:boxCornerRadiusTopStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusTopEnd="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomEnd="12dp"[m
[32m+[m[32m                app:boxBackgroundColor="@color/white">[m
[32m+[m
[32m+[m[32m                <com.google.android.material.textfield.TextInputEditText[m
[32m+[m[32m                    android:id="@+id/etPurchaseDate"[m
[32m+[m[32m                    android:layout_width="match_parent"[m
[32m+[m[32m                    android:layout_height="wrap_content"[m
[32m+[m[32m                    android:inputType="date"[m
[32m+[m[32m                    android:focusable="false"[m
[32m+[m[32m                    android:clickable="true"/>[m
[32m+[m
[32m+[m[32m            </com.google.android.material.textfield.TextInputLayout>[m
[32m+[m
[32m+[m[32m            <!-- Expiry Date -->[m
[32m+[m[32m            <com.google.android.material.textfield.TextInputLayout[m
[32m+[m[32m                android:layout_width="match_parent"[m
[32m+[m[32m                android:layout_height="wrap_content"[m
[32m+[m[32m                android:layout_marginBottom="16dp"[m
[32m+[m[32m                android:hint="Expiry Date (MM/DD/YYYY)"[m
[32m+[m[32m                app:boxCornerRadiusTopStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusTopEnd="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomEnd="12dp"[m
[32m+[m[32m                app:boxBackgroundColor="@color/white">[m
[32m+[m
[32m+[m[32m                <com.google.android.material.textfield.TextInputEditText[m
[32m+[m[32m                    android:id="@+id/etExpiryDate"[m
[32m+[m[32m                    android:layout_width="match_parent"[m
[32m+[m[32m                    android:layout_height="wrap_content"[m
[32m+[m[32m                    android:inputType="date"[m
[32m+[m[32m                    android:focusable="false"[m
[32m+[m[32m                    android:clickable="true"/>[m
[32m+[m
[32m+[m[32m            </com.google.android.material.textfield.TextInputLayout>[m
[32m+[m
[32m+[m[32m            <!-- Capacity -->[m
[32m+[m[32m            <com.google.android.material.textfield.TextInputLayout[m
[32m+[m[32m                android:layout_width="match_parent"[m
[32m+[m[32m                android:layout_height="wrap_content"[m
[32m+[m[32m                android:layout_marginBottom="16dp"[m
[32m+[m[32m                android:hint="Capacity"[m
[32m+[m[32m                app:boxCornerRadiusTopStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusTopEnd="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomEnd="12dp"[m
[32m+[m[32m                app:boxBackgroundColor="@color/white">[m
[32m+[m
[32m+[m[32m                <com.google.android.material.textfield.TextInputEditText[m
[32m+[m[32m                    android:id="@+id/etCapacity"[m
[32m+[m[32m                    android:layout_width="match_parent"[m
[32m+[m[32m                    android:layout_height="wrap_content"[m
[32m+[m[32m                    android:inputType="numberDecimal"/>[m
[32m+[m
[32m+[m[32m            </com.google.android.material.textfield.TextInputLayout>[m
[32m+[m
[32m+[m[32m            <!-- Remaining Amount -->[m
[32m+[m[32m            <com.google.android.material.textfield.TextInputLayout[m
[32m+[m[32m                android:layout_width="match_parent"[m
[32m+[m[32m                android:layout_height="wrap_content"[m
[32m+[m[32m                android:layout_marginBottom="16dp"[m
[32m+[m[32m                android:hint="Remaining Amount"[m
[32m+[m[32m                app:boxCornerRadiusTopStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusTopEnd="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomEnd="12dp"[m
[32m+[m[32m                app:boxBackgroundColor="@color/white">[m
[32m+[m
[32m+[m[32m                <com.google.android.material.textfield.TextInputEditText[m
[32m+[m[32m                    android:id="@+id/etRemainingAmount"[m
[32m+[m[32m                    android:layout_width="match_parent"[m
[32m+[m[32m                    android:layout_height="wrap_content"[m
[32m+[m[32m                    android:inputType="numberDecimal"/>[m
[32m+[m
[32m+[m[32m            </com.google.android.material.textfield.TextInputLayout>[m
[32m+[m
[32m+[m[32m            <!-- Label Dropdown -->[m
[32m+[m[32m            <com.google.android.material.textfield.TextInputLayout[m
[32m+[m[32m                android:layout_width="match_parent"[m
[32m+[m[32m                android:layout_height="wrap_content"[m
[32m+[m[32m                android:layout_marginBottom="16dp"[m
[32m+[m[32m                android:hint="Medicine Label"[m
[32m+[m[32m                app:boxCornerRadiusTopStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusTopEnd="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomEnd="12dp"[m
[32m+[m[32m                app:boxBackgroundColor="@color/white">[m
[32m+[m
[32m+[m[32m                <AutoCompleteTextView[m
[32m+[m[32m                    android:id="@+id/etMedicineLabel"[m
[32m+[m[32m                    android:layout_width="match_parent"[m
[32m+[m[32m                    android:layout_height="wrap_content"[m
[32m+[m[32m                    android:textSize="14sp"/>[m
[32m+[m
[32m+[m[32m            </com.google.android.material.textfield.TextInputLayout>[m
[32m+[m
[32m+[m[32m            <!-- Delete Button -->[m
[32m+[m[32m            <Button[m
[32m+[m[32m                android:id="@+id/btnDeleteMedicine"[m
[32m+[m[32m                android:layout_width="match_parent"[m
[32m+[m[32m                android:layout_height="wrap_content"[m
[32m+[m[32m                android:text="Delete Medicine"[m
[32m+[m[32m                android:backgroundTint="#D32F2F"[m
[32m+[m[32m                android:textColor="@color/white"[m
[32m+[m[32m                android:visibility="visible"/>[m
[32m+[m
[32m+[m[32m        </LinearLayout>[m
[32m+[m[32m    </ScrollView>[m
[32m+[m
[32m+[m[32m</androidx.constraintlayout.widget.ConstraintLayout>[m
[1mdiff --git a/app/src/main/res/layout/activity_inventory_log_history.xml b/app/src/main/res/layout/activity_inventory_log_history.xml[m
[1mindex cdc89f2..b640e75 100644[m
[1m--- a/app/src/main/res/layout/activity_inventory_log_history.xml[m
[1m+++ b/app/src/main/res/layout/activity_inventory_log_history.xml[m
[36m@@ -1,6 +1,118 @@[m
 <?xml version="1.0" encoding="utf-8"?>[m
 <androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"[m
[31m-             android:layout_width="match_parent"[m
[31m-             android:layout_height="match_parent">[m
[32m+[m[32m    xmlns:app="http://schemas.android.com/apk/res-auto"[m
[32m+[m[32m    xmlns:tools="http://schemas.android.com/tools"[m
[32m+[m[32m    android:layout_width="match_parent"[m
[32m+[m[32m    android:layout_height="match_parent"[m
[32m+[m[32m    android:background="#E8F5E9">[m
 [m
[31m-</androidx.constraintlayout.widget.ConstraintLayout>[m
\ No newline at end of file[m
[32m+[m[32m    <!-- Top Bar -->[m
[32m+[m[32m    <LinearLayout[m
[32m+[m[32m        android:id="@+id/topBar"[m
[32m+[m[32m        android:layout_width="match_parent"[m
[32m+[m[32m        android:layout_height="wrap_content"[m
[32m+[m[32m        android:orientation="horizontal"[m
[32m+[m[32m        android:background="#064200"[m
[32m+[m[32m        android:padding="16dp"[m
[32m+[m[32m        android:gravity="center_vertical"[m
[32m+[m[32m        app:layout_constraintTop_toTopOf="parent">[m
[32m+[m
[32m+[m[32m        <ImageButton[m
[32m+[m[32m            android:id="@+id/btnBack"[m
[32m+[m[32m            android:layout_width="48dp"[m
[32m+[m[32m            android:layout_height="48dp"[m
[32m+[m[32m            android:src="@android:drawable/ic_menu_revert"[m
[32m+[m[32m            android:background="?attr/selectableItemBackgroundBorderless"[m
[32m+[m[32m            android:contentDescription="Back"[m
[32m+[m[32m            tools:tint="@color/white"/>[m
[32m+[m
[32m+[m[32m        <TextView[m
[32m+[m[32m            android:id="@+id/titleText"[m
[32m+[m[32m            android:layout_width="0dp"[m
[32m+[m[32m            android:layout_height="wrap_content"[m
[32m+[m[32m            android:layout_weight="1"[m
[32m+[m[32m            android:text="Medicine Log History"[m
[32m+[m[32m            android:textSize="22sp"[m
[32m+[m[32m            android:textStyle="bold"[m
[32m+[m[32m            android:textColor="@color/white"[m
[32m+[m[32m            android:gravity="center"/>[m
[32m+[m[32m    </LinearLayout>[m
[32m+[m
[32m+[m[32m    <!-- Scroll Content -->[m
[32m+[m[32m    <ScrollView[m
[32m+[m[32m        android:id="@+id/scrollViewLogs"[m
[32m+[m[32m        android:layout_width="match_parent"[m
[32m+[m[32m        android:layout_height="0dp"[m
[32m+[m[32m        android:fillViewport="true"[m
[32m+[m[32m        app:layout_constraintTop_toBottomOf="@id/topBar"[m
[32m+[m[32m        app:layout_constraintBottom_toTopOf="@+id/bottomToggleBar">[m
[32m+[m
[32m+[m[32m        <LinearLayout[m
[32m+[m[32m            android:id="@+id/containerLogs"[m
[32m+[m[32m            android:layout_width="match_parent"[m
[32m+[m[32m            android:layout_height="wrap_content"[m
[32m+[m[32m            android:orientation="vertical"[m
[32m+[m[32m            android:padding="20dp">[m
[32m+[m
[32m+[m[32m            <!-- Example Log Entry Block (You will populate dynamically) -->[m
[32m+[m[32m            <LinearLayout[m
[32m+[m[32m                android:id="@+id/exampleEntry"[m
[32m+[m[32m                android:layout_width="match_parent"[m
[32m+[m[32m                android:layout_height="wrap_content"[m
[32m+[m[32m                android:orientation="vertical"[m
[32m+[m[32m                android:padding="16dp"[m
[32m+[m[32m                android:background="@color/white"[m
[32m+[m[32m                android:elevation="4dp"[m
[32m+[m[32m                android:layout_marginBottom="12dp"[m
[32m+[m[32m                android:clipToPadding="false"[m
[32m+[m[32m                android:backgroundTint="@color/white">[m
[32m+[m
[32m+[m[32m                <TextView[m
[32m+[m[32m                    android:id="@+id/tvLogLine"[m
[32m+[m[32m                    android:layout_width="wrap_content"[m
[32m+[m[32m                    android:layout_height="wrap_content"[m
[32m+[m[32m                    android:text="Medicine: ExampleMed, Dosage: 2, Timestamp: 2025-11-26 00:00"[m
[32m+[m[32m                    android:textSize="16sp"[m
[32m+[m[32m                    android:textColor="#064200"/>[m
[32m+[m[32m            </LinearLayout>[m
[32m+[m
[32m+[m[32m        </LinearLayout>[m
[32m+[m
[32m+[m[32m    </ScrollView>[m
[32m+[m
[32m+[m[32m    <!-- Bottom Toggle Bar -->[m
[32m+[m[32m    <LinearLayout[m
[32m+[m[32m        android:id="@+id/bottomToggleBar"[m
[32m+[m[32m        android:layout_width="match_parent"[m
[32m+[m[32m        android:layout_height="wrap_content"[m
[32m+[m[32m        android:padding="16dp"[m
[32m+[m[32m        android:gravity="center"[m
[32m+[m[32m        android:orientation="horizontal"[m
[32m+[m[32m        android:background="#064200"[m
[32m+[m[32m        app:layout_constraintBottom_toBottomOf="parent">[m
[32m+[m
[32m+[m[32m        <TextView[m
[32m+[m[32m            android:id="@+id/tvControllerLabel"[m
[32m+[m[32m            android:layout_width="wrap_content"[m
[32m+[m[32m            android:layout_height="wrap_content"[m
[32m+[m[32m            android:text="Controller"[m
[32m+[m[32m            android:textSize="16sp"[m
[32m+[m[32m            android:textColor="@color/white"[m
[32m+[m[32m            android:layout_marginEnd="12dp"/>[m
[32m+[m
[32m+[m[32m        <com.google.android.material.materialswitch.MaterialSwitch[m
[32m+[m[32m            android:id="@+id/switchLogFilter"[m
[32m+[m[32m            android:layout_width="wrap_content"[m
[32m+[m[32m            android:layout_height="wrap_content"/>[m
[32m+[m
[32m+[m[32m        <TextView[m
[32m+[m[32m            android:id="@+id/tvRescueLabel"[m
[32m+[m[32m            android:layout_width="wrap_content"[m
[32m+[m[32m            android:layout_height="wrap_content"[m
[32m+[m[32m            android:text="Rescue"[m
[32m+[m[32m            android:textSize="16sp"[m
[32m+[m[32m            android:textColor="@color/white"[m
[32m+[m[32m            android:layout_marginStart="12dp"/>[m
[32m+[m[32m    </LinearLayout>[m
[32m+[m
[32m+[m[32m</androidx.constraintlayout.widget.ConstraintLayout>[m
[1mdiff --git a/app/src/main/res/layout/activity_inventory_usage.xml b/app/src/main/res/layout/activity_inventory_usage.xml[m
[1mnew file mode 100644[m
[1mindex 0000000..7f80bb6[m
[1m--- /dev/null[m
[1m+++ b/app/src/main/res/layout/activity_inventory_usage.xml[m
[36m@@ -0,0 +1,131 @@[m
[32m+[m[32m<?xml version="1.0" encoding="utf-8"?>[m
[32m+[m[32m<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"[m
[32m+[m[32m    xmlns:app="http://schemas.android.com/apk/res-auto"[m
[32m+[m[32m    xmlns:tools="http://schemas.android.com/tools"[m
[32m+[m[32m    android:layout_width="match_parent"[m
[32m+[m[32m    android:layout_height="match_parent"[m
[32m+[m[32m    android:background="#E8F5E9">[m
[32m+[m
[32m+[m[32m    <!-- Top Bar -->[m
[32m+[m[32m    <LinearLayout[m
[32m+[m[32m        android:id="@+id/topBar"[m
[32m+[m[32m        android:layout_width="match_parent"[m
[32m+[m[32m        android:layout_height="wrap_content"[m
[32m+[m[32m        android:orientation="horizontal"[m
[32m+[m[32m        android:background="#064200"[m
[32m+[m[32m        android:padding="16dp"[m
[32m+[m[32m        android:gravity="center_vertical"[m
[32m+[m[32m        app:layout_constraintTop_toTopOf="parent">[m
[32m+[m
[32m+[m[32m        <ImageButton[m
[32m+[m[32m            android:id="@+id/btnBack"[m
[32m+[m[32m            android:layout_width="48dp"[m
[32m+[m[32m            android:layout_height="48dp"[m
[32m+[m[32m            android:src="@android:drawable/ic_menu_revert"[m
[32m+[m[32m            android:background="?attr/selectableItemBackgroundBorderless"[m
[32m+[m[32m            android:contentDescription="Back"[m
[32m+[m[32m            tools:tint="@color/white"/>[m
[32m+[m
[32m+[m[32m        <TextView[m
[32m+[m[32m            android:id="@+id/titleText"[m
[32m+[m[32m            android:layout_width="0dp"[m
[32m+[m[32m            android:layout_height="wrap_content"[m
[32m+[m[32m            android:layout_weight="1"[m
[32m+[m[32m            android:text="Medicine Usage"[m
[32m+[m[32m            android:textSize="22sp"[m
[32m+[m[32m            android:textStyle="bold"[m
[32m+[m[32m            android:textColor="@color/white"[m
[32m+[m[32m            android:gravity="center"/>[m
[32m+[m
[32m+[m[32m        <Button[m
[32m+[m[32m            android:id="@+id/btnSaveMedicine"[m
[32m+[m[32m            android:layout_width="wrap_content"[m
[32m+[m[32m            android:layout_height="wrap_content"[m
[32m+[m[32m            android:text="Save"[m
[32m+[m[32m            android:backgroundTint="@color/white"[m
[32m+[m[32m            android:textColor="#064200"/>[m
[32m+[m
[32m+[m[32m    </LinearLayout>[m
[32m+[m
[32m+[m[32m    <!-- Body Scroll -->[m
[32m+[m[32m    <ScrollView[m
[32m+[m[32m        android:layout_width="match_parent"[m
[32m+[m[32m        android:layout_height="0dp"[m
[32m+[m[32m        app:layout_constraintTop_toBottomOf="@id/topBar"[m
[32m+[m[32m        app:layout_constraintBottom_toBottomOf="parent">[m
[32m+[m
[32m+[m[32m        <LinearLayout[m
[32m+[m[32m            android:layout_width="match_parent"[m
[32m+[m[32m            android:layout_height="wrap_content"[m
[32m+[m[32m            android:orientation="vertical"[m
[32m+[m[32m            android:padding="24dp">[m
[32m+[m
[32m+[m[32m            <!-- Date Input -->[m
[32m+[m[32m            <com.google.android.material.textfield.TextInputLayout[m
[32m+[m[32m                android:layout_width="match_parent"[m
[32m+[m[32m                android:layout_height="wrap_content"[m
[32m+[m[32m                android:layout_marginBottom="16dp"[m
[32m+[m[32m                android:hint="Date (MM/DD/YYYY)"[m
[32m+[m[32m                app:boxBackgroundColor="@color/white"[m
[32m+[m[32m                app:boxCornerRadiusTopStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusTopEnd="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomEnd="12dp">[m
[32m+[m
[32m+[m[32m                <com.google.android.material.textfield.TextInputEditText[m
[32m+[m[32m                    android:id="@+id/etDate"[m
[32m+[m[32m                    android:layout_width="match_parent"[m
[32m+[m[32m                    android:layout_height="wrap_content"[m
[32m+[m[32m                    android:inputType="date"[m
[32m+[m[32m                    android:focusable="false"[m
[32m+[m[32m                    android:clickable="true"/>[m
[32m+[m
[32m+[m[32m            </com.google.android.material.textfield.TextInputLayout>[m
[32m+[m
[32m+[m[32m            <!-- Time Input -->[m
[32m+[m[32m            <com.google.android.material.textfield.TextInputLayout[m
[32m+[m[32m                android:layout_width="match_parent"[m
[32m+[m[32m                android:layout_height="wrap_content"[m
[32m+[m[32m                android:layout_marginBottom="16dp"[m
[32m+[m[32m                android:hint="Time Taken"[m
[32m+[m[32m                app:boxBackgroundColor="@color/white"[m
[32m+[m[32m                app:boxCornerRadiusTopStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusTopEnd="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomEnd="12dp">[m
[32m+[m
[32m+[m[32m                <com.google.android.material.textfield.TextInputEditText[m
[32m+[m[32m                    android:id="@+id/etTime"[m
[32m+[m[32m                    android:layout_width="match_parent"[m
[32m+[m[32m                    android:layout_height="wrap_content"[m
[32m+[m[32m                    android:inputType="time"[m
[32m+[m[32m                    android:focusable="false"[m
[32m+[m[32m                    android:clickable="true"/>[m
[32m+[m
[32m+[m[32m            </com.google.android.material.textfield.TextInputLayout>[m
[32m+[m
[32m+[m[32m            <!-- Dosage Input -->[m
[32m+[m[32m            <com.google.android.material.textfield.TextInputLayout[m
[32m+[m[32m                android:layout_width="match_parent"[m
[32m+[m[32m                android:layout_height="wrap_content"[m
[32m+[m[32m                android:hint="Dosage Amount"[m
[32m+[m[32m                android:layout_marginBottom="16dp"[m
[32m+[m[32m                app:boxBackgroundColor="@color/white"[m
[32m+[m[32m                app:boxCornerRadiusTopStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusTopEnd="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomStart="12dp"[m
[32m+[m[32m                app:boxCornerRadiusBottomEnd="12dp">[m
[32m+[m
[32m+[m[32m                <com.google.android.material.textfield.TextInputEditText[m
[32m+[m[32m                    android:id="@+id/etDosage"[m
[32m+[m[32m                    android:layout_width="match_parent"[m
[32m+[m[32m                    android:layout_height="wrap_content"[m
[32m+[m[32m                    android:inputType="numberDecimal"/>[m
[32m+[m
[32m+[m[32m            </com.google.android.material.textfield.TextInputLayout>[m
[32m+[m
[32m+[m[32m        </LinearLayout>[m
[32m+[m
[32m+[m[32m    </ScrollView>[m
[32m+[m
[32m+[m[32m</androidx.constraintlayout.widget.ConstraintLayout>[m
\ No newline at end of file[m
