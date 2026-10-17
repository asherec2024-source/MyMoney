package com.mymoney.app

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

private val green = Color(0xFF087F5B)
private fun money(p:Long) = "₹%.2f".format(p/100.0)
private fun paise(s:String) = ((s.toDoubleOrNull() ?: 0.0)*100).toLong()
class MoneyVm(val db:MoneyDatabase):ViewModel() {
 val dao=db.dao()
 val plans=dao.plans().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 fun savePlan(p:FinancePlan)=viewModelScope.launch {if(p.id==0L)dao.addPlan(p) else dao.updatePlan(p)}
 fun deletePlan(p:FinancePlan)=viewModelScope.launch {dao.deletePlan(p)}
 val accounts=dao.accounts().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val methods=dao.methods().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val transactions=dao.transactions().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 fun saveAccount(a:Account)=viewModelScope.launch { if(a.id==0L)dao.addAccount(a) else dao.updateAccount(a) }
 fun deleteAccount(a:Account,notify:(String)->Unit)=viewModelScope.launch {if(dao.accountUsage(a.id)==0){dao.deleteAccount(a);notify("Account deleted")}else notify("Account has transactions; delete them first")}
 fun saveMethod(m:PaymentMethod)=viewModelScope.launch {if(m.id==0L)dao.addMethod(m) else dao.updateMethod(m)}
 fun deleteMethod(m:PaymentMethod,notify:(String)->Unit)=viewModelScope.launch {if(dao.methodUsage(m.id)==0){dao.deleteMethod(m);notify("Payment method deleted")}else notify("Method is used by transactions")}
 fun saveTransaction(t:MoneyTransaction)=viewModelScope.launch {if(t.id==0L)dao.addTransaction(t) else dao.updateTransaction(t)}
 fun deleteTransaction(t:MoneyTransaction)=viewModelScope.launch {dao.deleteTransaction(t)}
}
class MainActivity:FragmentActivity() {
 private val unlocked = mutableStateOf(false)
 private val lockEnabled = mutableStateOf(false)
 private var authenticationShowing = false
 private fun canUnlock():Boolean {
  val allowed = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
  return BiometricManager.from(this).canAuthenticate(allowed)==BiometricManager.BIOMETRIC_SUCCESS
 }
 private fun authenticate() {
  if(authenticationShowing || !canUnlock()) return
  authenticationShowing=true
  val allowed=BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
  BiometricPrompt(this,ContextCompat.getMainExecutor(this),object:BiometricPrompt.AuthenticationCallback(){
   override fun onAuthenticationSucceeded(result:BiometricPrompt.AuthenticationResult) {
    super.onAuthenticationSucceeded(result)
    authenticationShowing=false
    unlocked.value=true
   }
   override fun onAuthenticationError(errorCode:Int,errString:CharSequence) {
    super.onAuthenticationError(errorCode,errString)
    authenticationShowing=false
   }
  }).authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Unlock MyMoney")
    .setSubtitle("Use fingerprint, face or your device screen lock")
    .setAllowedAuthenticators(allowed).build())
 }
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  lockEnabled.value=getSharedPreferences("security",MODE_PRIVATE).getBoolean("app_lock",false)
  unlocked.value=!lockEnabled.value
  val db=Room.databaseBuilder(applicationContext,MoneyDatabase::class.java,"mymoney.db")
   .addMigrations(MIGRATION_1_2).build()
  setContent {
   val vm:MoneyVm=viewModel(factory=object:ViewModelProvider.Factory {
    override fun <T:ViewModel> create(modelClass:Class<T>):T {
     @Suppress("UNCHECKED_CAST")
     return MoneyVm(db) as T
    }
   })
   MaterialTheme(colorScheme=lightColorScheme(primary=green)) {
    if(unlocked.value) {
     MoneyApp(vm,lockEnabled.value,onLockChange={enabled->
      if(enabled) {
       if(canUnlock()){
        getSharedPreferences("security",MODE_PRIVATE).edit().putBoolean("app_lock",true).apply()
        lockEnabled.value=true
       } else android.widget.Toast.makeText(this,"Set up fingerprint or device screen lock first",android.widget.Toast.LENGTH_LONG).show()
      } else {
       getSharedPreferences("security",MODE_PRIVATE).edit().putBoolean("app_lock",false).apply()
       lockEnabled.value=false
      }
     },onLockNow={
      if(lockEnabled.value) { unlocked.value=false;authenticate() }
     })
    } else {
     androidx.compose.foundation.layout.Box(
      androidx.compose.ui.Modifier.fillMaxSize(),
      contentAlignment=androidx.compose.ui.Alignment.Center
     ) {
      androidx.compose.material3.Button(onClick={authenticate()}) {
       androidx.compose.material3.Text("Unlock MyMoney")
      }
     }
    }
   }
  }
  if(lockEnabled.value) window.decorView.post{authenticate()}
 }
 override fun onStop(){
  super.onStop()
  if(lockEnabled.value && !authenticationShowing) unlocked.value=false
 }
 override fun onResume(){
  super.onResume()
  if(lockEnabled.value && !unlocked.value) window.decorView.post{authenticate()}
 }
}
@Composable fun EntryCard(title:String,detail:String,onEdit:()->Unit,onDelete:()->Unit){Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text(title,style=MaterialTheme.typography.titleMedium);Text(detail);Row{TextButton(onClick=onEdit){Text("Edit")};TextButton(onClick=onDelete){Text("Delete")}}}}}
@Composable fun AccountDialog(old:Account?,onClose:()->Unit,onSave:(Account)->Unit){
 var name by remember(old){mutableStateOf(old?.name?:"")}
 var type by remember(old){mutableStateOf(old?.type?:"Bank")}
 var opening by remember(old){mutableStateOf(((old?.openingPaise?:0)/100.0).toString())}
 var limit by remember(old){mutableStateOf(((old?.creditLimitPaise?:0)/100.0).toString())}
 var statement by remember(old){mutableStateOf((old?.statementDay?:1).toString())}
 var due by remember(old){mutableStateOf((old?.dueDay?:1).toString())}
 AlertDialog(onDismissRequest=onClose,title={Text(if(old==null)"Add account" else "Edit account")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
 OutlinedTextField(name,{name=it},label={Text("Account name")})
 Row(Modifier.horizontalScroll(rememberScrollState())){listOf("Bank","Cash Wallet","Credit Card").forEach{t->FilterChip(selected=type==t,onClick={type=t},label={Text(t)},modifier=Modifier.padding(end=5.dp))}}
 OutlinedTextField(opening,{opening=it},label={Text("Opening balance ₹")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))
 if(type=="Credit Card"){OutlinedTextField(limit,{limit=it},label={Text("Credit limit ₹")});OutlinedTextField(statement,{statement=it},label={Text("Statement day")});OutlinedTextField(due,{due=it},label={Text("Due day")})}
 }},confirmButton={TextButton(enabled=name.isNotBlank(),onClick={onSave(Account(old?.id?:0,name.trim(),type,paise(opening),paise(limit),statement.toIntOrNull()?.coerceIn(1,31)?:1,due.toIntOrNull()?.coerceIn(1,31)?:1))}){Text("Save")}},dismissButton={TextButton(onClick=onClose){Text("Cancel")}})
}
@Composable fun MethodDialog(old:PaymentMethod?,onClose:()->Unit,onSave:(PaymentMethod)->Unit){
 var name by remember(old){mutableStateOf(old?.name?:"")};var details by remember(old){mutableStateOf(old?.details?:"")}
 AlertDialog(onDismissRequest=onClose,title={Text("Payment method")},text={Column{OutlinedTextField(name,{name=it},label={Text("Name (UPI, cash, debit card...)")});OutlinedTextField(details,{details=it},label={Text("Details")})}},confirmButton={TextButton(enabled=name.isNotBlank(),onClick={onSave(PaymentMethod(old?.id?:0,name.trim(),details))}){Text("Save")}},dismissButton={TextButton(onClick=onClose){Text("Cancel")}})
}
@Composable fun TransactionDialog(old:MoneyTransaction?,accounts:List<Account>,methods:List<PaymentMethod>,onClose:()->Unit,onSave:(MoneyTransaction)->Unit){
 var kind by remember(old){mutableStateOf(old?.kind?:"Expense")}
 var amount by remember(old){mutableStateOf(((old?.amountPaise?:0)/100.0).toString())}
 var category by remember(old){mutableStateOf(old?.category?:"")}
 var account by remember(old){mutableLongStateOf(old?.accountId?:accounts.firstOrNull()?.id?:0L)}
 var method by remember(old){mutableLongStateOf(old?.paymentMethodId?:0L)}
 var note by remember(old){mutableStateOf(old?.note?:"")}
 AlertDialog(onDismissRequest=onClose,title={Text("Transaction")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(6.dp)){
 Row{listOf("Expense","Income").forEach{t->FilterChip(selected=kind==t,onClick={kind=t},label={Text(t)},modifier=Modifier.padding(end=8.dp))}}
 OutlinedTextField(amount,{amount=it},label={Text("Amount ₹")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))
 OutlinedTextField(category,{category=it},label={Text("Category")})
 Text("Account");accounts.forEach{a->Row(verticalAlignment=Alignment.CenterVertically){RadioButton(selected=account==a.id,onClick={account=a.id});Text(a.name)}}
 Text("Payment method (optional)");Row(verticalAlignment=Alignment.CenterVertically){RadioButton(selected=method==0L,onClick={method=0L});Text("None")}
 methods.forEach{m->Row(verticalAlignment=Alignment.CenterVertically){RadioButton(selected=method==m.id,onClick={method=m.id});Text(m.name)}}
 OutlinedTextField(note,{note=it},label={Text("Note")})
 }},confirmButton={TextButton(enabled=account!=0L&&paise(amount)>0&&category.isNotBlank(),onClick={onSave(MoneyTransaction(old?.id?:0,kind,paise(amount),category.trim(),account,method.takeIf{it!=0L},old?.date?:LocalDate.now().toString(),note))}){Text("Save")}},dismissButton={TextButton(onClick=onClose){Text("Cancel")}})
}
