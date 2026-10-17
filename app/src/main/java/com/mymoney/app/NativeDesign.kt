package com.mymoney.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

private val emerald = Color(0xFF087F69)
private val forest = Color(0xFF0D5349)
private val ink = Color(0xFF17332B)
private val muted = Color(0xFF778B82)
private val canvas = Color(0xFFF4F8F6)
private val pale = Color(0xFFE5F5ED)
private val line = Color(0xFFE4EEE8)
private fun rupees(v:Long) = "₹%,.2f".format(v/100.0)
private fun toPaise(v:String) = ((v.toDoubleOrNull()?:0.0)*100).toLong()
private val tabs = listOf(Triple("Home","⌂","Home"),Triple("Accounts","▣","Accounts"),Triple("Cards","▤","Cards"),Triple("Activity","☷","Activity"),Triple("EMIs","▦","EMIs"),Triple("Budgets","▥","Budgets"),Triple("Goals","◎","Goals"),Triple("Reports","▥","Reports"))

@Composable private fun Heading(text:String){Text(text,fontSize=17.sp,fontWeight=FontWeight.ExtraBold,color=ink,modifier=Modifier.padding(top=9.dp,bottom=2.dp))}
@Composable private fun Panel(content:@Composable ColumnScope.()->Unit){
 Column(Modifier.fillMaxWidth().border(1.dp,line,RoundedCornerShape(18.dp)).background(Color.White,RoundedCornerShape(18.dp)).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp),content=content)
}
@Composable private fun Pill(text:String){Text(text,color=emerald,fontSize=11.sp,fontWeight=FontWeight.Bold,modifier=Modifier.background(pale,RoundedCornerShape(30.dp)).padding(horizontal=10.dp,vertical=6.dp))}
@Composable private fun SmallButton(text:String,onClick:()->Unit){TextButton(onClick=onClick,contentPadding=PaddingValues(horizontal=9.dp,vertical=2.dp)){Text(text,color=emerald,fontSize=12.sp,fontWeight=FontWeight.Bold)}}
@Composable private fun Empty(text:String){Panel{Text(text,color=muted,fontSize=13.sp)}}
@Composable private fun Metric(label:String,value:String,modifier:Modifier=Modifier){Column(modifier.border(1.dp,line,RoundedCornerShape(17.dp)).background(Color.White,RoundedCornerShape(17.dp)).padding(15.dp)){Text(label,color=muted,fontSize=12.sp);Spacer(Modifier.height(6.dp));Text(value,color=ink,fontWeight=FontWeight.ExtraBold,fontSize=19.sp)}}
@Composable private fun ItemRow(symbol:String,title:String,subtitle:String,amount:String?=null,actions:@Composable RowScope.()->Unit={}){
 Row(Modifier.fillMaxWidth().padding(vertical=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
 Box(Modifier.size(43.dp).background(pale,RoundedCornerShape(13.dp)),contentAlignment=Alignment.Center){Text(symbol,fontSize=21.sp,color=emerald)}
 Column(Modifier.weight(1f)){Text(title,color=ink,fontWeight=FontWeight.SemiBold,fontSize=14.sp,maxLines=1,overflow=TextOverflow.Ellipsis);Text(subtitle,color=muted,fontSize=11.sp)}
 if(amount!=null)Text(amount,color=ink,fontWeight=FontWeight.Bold,fontSize=13.sp)
 actions()
 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MoneyApp(vm:MoneyVm,lockEnabled:Boolean,onLockChange:(Boolean)->Unit){
 val accounts by vm.accounts.collectAsState()
 val methods by vm.methods.collectAsState()
 val tx by vm.transactions.collectAsState()
 val plans by vm.plans.collectAsState()
 var tab by remember{mutableStateOf("Home")}
 var quick by remember{mutableStateOf(false)}
 var editor by remember{mutableStateOf("")}
 var editAccount by remember{mutableStateOf<Account?>(null)}
 var editMethod by remember{mutableStateOf<PaymentMethod?>(null)}
 var editTx by remember{mutableStateOf<MoneyTransaction?>(null)}
 var editPlan by remember{mutableStateOf<FinancePlan?>(null)}
 var toast by remember{mutableStateOf("")}
 var exitDialog by remember{mutableStateOf(false)}
 val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
 BackHandler(enabled=!quick && editor.isEmpty()) {
     if(tab!="Home" && tab!="Settings") tab="Home" else exitDialog=true
 }
 val income=tx.filter{it.kind=="Income"}.sumOf{it.amountPaise}
 val expenses=tx.filter{it.kind=="Expense"}.sumOf{it.amountPaise}
 val balance=accounts.sumOf{a->a.openingPaise+tx.filter{it.accountId==a.id}.sumOf{if(it.kind=="Income")it.amountPaise else -it.amountPaise}}
 val planKind=when(tab){"Cards"->"Card";"EMIs"->"EMI";"Budgets"->"Budget";"Goals"->"Goal";else->""}
 Scaffold(containerColor=canvas,bottomBar={
 Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal=2.dp,vertical=7.dp),horizontalArrangement=Arrangement.SpaceEvenly){
 tabs.forEach{(id,symbol,label)->
 Column(Modifier.weight(1f).clickable{tab=id}.padding(vertical=5.dp),horizontalAlignment=Alignment.CenterHorizontally){
 Text(symbol,fontSize=22.sp,color=if(tab==id)emerald else muted)
 Text(label,fontSize=8.sp,maxLines=1,fontWeight=FontWeight.Bold,color=if(tab==id)emerald else muted)
 }
 }
 }
 },floatingActionButton={
 FloatingActionButton(onClick={quick=true},shape=RoundedCornerShape(19.dp),containerColor=emerald,contentColor=Color.White){Text("+",fontSize=30.sp)}
 }){
 padding->
 LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(start=18.dp,end=18.dp,top=18.dp,bottom=95.dp),verticalArrangement=Arrangement.spacedBy(11.dp)){
 item{
 Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
 Box(Modifier.size(43.dp).background(pale,RoundedCornerShape(13.dp)),contentAlignment=Alignment.Center){Text("M",fontWeight=FontWeight.Black,fontSize=22.sp,color=emerald)}
 Spacer(Modifier.width(11.dp))
 Column(Modifier.weight(1f)){Text("Welcome back 👋",color=muted,fontSize=12.sp);Text("MyMoney",color=ink,fontWeight=FontWeight.ExtraBold,fontSize=20.sp)}
 SmallButton("⚙ Settings"){tab="Settings"}
 }
 }
 if(toast.isNotEmpty())item{Pill(toast)}
 when(tab){
 "Home"->{
 item{
 Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(emerald,forest)),RoundedCornerShape(22.dp)).padding(23.dp)){
 Text("Total available balance",color=Color.White.copy(alpha=.8f),fontSize=13.sp)
 Spacer(Modifier.height(7.dp))
 Text(rupees(balance),color=Color.White,fontWeight=FontWeight.Black,fontSize=32.sp)
 Text("Across ${accounts.size} accounts",color=Color.White.copy(alpha=.8f),fontSize=12.sp)
 Spacer(Modifier.height(17.dp))
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
 Button(onClick={editor="transaction"},colors=ButtonDefaults.buttonColors(containerColor=Color.White.copy(alpha=.19f))){Text("＋ Add transaction",fontSize=11.sp)}
 OutlinedButton(onClick={tab="Accounts"},colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White)){Text("Accounts →",fontSize=11.sp)}
 }
 }
 }
 item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Metric("↗ Income",rupees(income),Modifier.weight(1f));Metric("↘ Expenses",rupees(expenses),Modifier.weight(1f))}}
 item{Heading("Spending overview")}
 item{Panel{Text("Total recorded expenses",color=muted,fontSize=12.sp);Text(rupees(expenses),color=ink,fontSize=25.sp,fontWeight=FontWeight.Bold);HorizontalDivider(color=line);Text("Live totals from your offline database",color=muted,fontSize=11.sp)}}
 item{Heading("Quick actions")}
 item{Row(horizontalArrangement=Arrangement.spacedBy(9.dp)){listOf("Expense","Income","Account").forEach{label->Column(Modifier.weight(1f).border(1.dp,line,RoundedCornerShape(14.dp)).background(Color.White,RoundedCornerShape(14.dp)).clickable{editor=if(label=="Account")"account" else "transaction"}.padding(15.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(when(label){"Expense"->"🛍️";"Income"->"💰";else->"🏦"},fontSize=22.sp);Text(label,fontSize=12.sp,color=ink)}}}}
 item{Heading("Recent activity")}
 if(tx.isEmpty())item{Empty("No transactions yet. Tap + to record your first one.")}
 items(tx.take(5)){t->Panel{ItemRow(if(t.kind=="Income")"↗" else "↘",t.category,t.date,rupees(t.amountPaise))}}
 }
 "Accounts"->{item{Heading("Accounts");SmallButton("＋ Add account"){editAccount=null;editor="account"}}
 if(accounts.isEmpty())item{Empty("No accounts yet. Add a bank or cash account.")}
 items(accounts){a->Panel{ItemRow("🏦",a.name,a.type,rupees(a.openingPaise));Row{SmallButton("Edit"){editAccount=a;editor="account"};SmallButton("Delete"){vm.deleteAccount(a){toast=it}}}}}
 item{Heading("Payment methods");SmallButton("＋ Add method"){editMethod=null;editor="method"}}
 if(methods.isEmpty())item{Empty("Add UPI, debit card, cash, or another payment method.")}
 items(methods){m->Panel{ItemRow("💳",m.name,m.details);Row{SmallButton("Edit"){editMethod=m;editor="method"};SmallButton("Delete"){vm.deleteMethod(m){toast=it}}}}}
 }
 "Activity"->{item{Heading("Activity");SmallButton("＋ Add transaction"){editTx=null;editor="transaction"}}
 if(tx.isEmpty())item{Empty("Your transaction history will appear here.")}
 items(tx){t->Panel{ItemRow(if(t.kind=="Income")"↗" else "↘",t.category,"${t.kind} · ${t.date}",rupees(t.amountPaise));Row{SmallButton("Edit"){editTx=t;editor="transaction"};SmallButton("Delete"){vm.deleteTransaction(t)}}}}
 }
 "Cards","EMIs","Budgets","Goals"->{
 item{Heading(tab);SmallButton("＋ Add ${planKind.lowercase()}"){editPlan=null;editor="plan"}}
 val matching=plans.filter{it.kind==planKind}
 if(matching.isEmpty())item{Empty("No ${tab.lowercase()} yet. Add one to get started.")}
 items(matching){p->Panel{
 ItemRow(when(p.kind){"Card"->"💳";"EMI"->"📅";"Budget"->"📊";else->"🎯"},p.name,when(p.kind){"Card"->"Credit limit";"EMI"->"${p.months} months · day ${p.day}";"Budget"->"Spending budget";else->"Savings target"},rupees(p.amountPaise))
 if(p.kind=="Goal"||p.kind=="Budget"){val progress=if(p.kind=="Goal")p.progressPaise else tx.filter{it.kind=="Expense"&&it.category.equals(p.name,true)}.sumOf{it.amountPaise}
 Text("${rupees(progress)} / ${rupees(p.amountPaise)}",fontSize=12.sp,color=muted)
 LinearProgressIndicator(progress={if(p.amountPaise>0)(progress.toFloat()/p.amountPaise).coerceIn(0f,1f) else 0f},modifier=Modifier.fillMaxWidth(),color=emerald,trackColor=pale)
 }
 Row{SmallButton("Edit"){editPlan=p;editor="plan"};SmallButton("Delete"){vm.deletePlan(p)}}
 }}
 }
 "Reports"->{
 item{Heading("Reports")}
 item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Metric("Income",rupees(income),Modifier.weight(1f));Metric("Expenses",rupees(expenses),Modifier.weight(1f))}}
 item{Panel{Text("Net cash flow",color=muted);Text(rupees(income-expenses),fontSize=27.sp,fontWeight=FontWeight.Bold,color=emerald)}}
 item{Heading("Spending by category")}
 val categories=tx.filter{it.kind=="Expense"}.groupBy{it.category}
 if(categories.isEmpty())item{Empty("Record expenses to see category reports.")}
 items(categories.entries.toList()){e->Panel{ItemRow("▥",e.key,"${e.value.size} transactions",rupees(e.value.sumOf{it.amountPaise}))}}
 }
 "Settings"->{item{Heading("Settings");Panel{Text("Offline native Android app",fontWeight=FontWeight.Bold,color=ink);Text("Data is stored locally using Room/SQLite. No demo records are inserted.",color=muted,fontSize=12.sp);Text("Backup and import are not implemented yet.",color=muted,fontSize=12.sp);Row(verticalAlignment=Alignment.CenterVertically){Text("App lock (biometrics / screen lock)",modifier=Modifier.weight(1f),color=ink,fontSize=13.sp);Switch(checked=lockEnabled,onCheckedChange=onLockChange)}};SmallButton("Payment methods"){tab="Accounts"}}}
 }
 }
 }
 if(exitDialog)AlertDialog(onDismissRequest={exitDialog=false},title={Text("Exit MyMoney?")},text={Text("Do you want to close MyMoney?")},confirmButton={TextButton(onClick={exitDialog=false; activity?.finish()}){Text("Exit")}},dismissButton={TextButton(onClick={exitDialog=false}){Text("Cancel")}})
 if(quick)AlertDialog(onDismissRequest={quick=false},title={Text("Quick actions")},text={Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
 listOf("Expense" to "transaction","Income" to "transaction","Transfer" to "transfer","ATM cash" to "withdrawal","Add EMI" to "EMIs","Add card" to "Cards","Budget" to "Budgets","Goal" to "Goals","Account" to "account","Payment method" to "method").forEach{(label,target)->TextButton(onClick={quick=false;if(target in listOf("EMIs","Cards","Budgets","Goals")){tab=target;editPlan=null;editor="plan"}else if(target=="transfer"||target=="withdrawal"){toast="Transfer and ATM accounting are coming in a later update"}else{editor=target;if(target=="account")editAccount=null;if(target=="method")editMethod=null;if(target=="transaction")editTx=null}}){Text(label,color=emerald)}}
 }},confirmButton={TextButton(onClick={quick=false}){Text("Close")}})
 if(editor=="account")AccountDialog(editAccount,onClose={editor=""},onSave={vm.saveAccount(it);editor=""})
 if(editor=="method")MethodDialog(editMethod,onClose={editor=""},onSave={vm.saveMethod(it);editor=""})
 if(editor=="transaction")TransactionDialog(editTx,accounts,methods,onClose={editor=""},onSave={vm.saveTransaction(it);editor=""})
 if(editor=="plan")PlanDialog(planKind,editPlan,onClose={editor=""},onSave={vm.savePlan(it);editor=""})
}

@Composable private fun PlanDialog(kind:String,old:FinancePlan?,onClose:()->Unit,onSave:(FinancePlan)->Unit){
 var name by remember(old,kind){mutableStateOf(old?.name?:"")}
 var amount by remember(old,kind){mutableStateOf(old?.let{(it.amountPaise/100.0).toString()}?:"")}
 var progress by remember(old,kind){mutableStateOf(old?.let{(it.progressPaise/100.0).toString()}?:"0")}
 var day by remember(old,kind){mutableStateOf((old?.day?:1).toString())}
 var months by remember(old,kind){mutableStateOf((old?.months?:12).toString())}
 AlertDialog(onDismissRequest=onClose,title={Text(if(old==null)"Add $kind" else "Edit $kind")},text={
 Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
 OutlinedTextField(name,{name=it},label={Text("Name")},singleLine=true)
 OutlinedTextField(amount,{amount=it},label={Text(when(kind){"Card"->"Credit limit ₹";"EMI"->"Monthly EMI ₹";"Budget"->"Monthly budget ₹";else->"Target amount ₹"})},singleLine=true)
 if(kind=="Goal")OutlinedTextField(progress,{progress=it},label={Text("Already saved ₹")})
 if(kind=="EMI"){OutlinedTextField(day,{day=it},label={Text("Payment day (1–28)")});OutlinedTextField(months,{months=it},label={Text("Number of months")})}
 if(kind=="Card")Text("Statement and due dates will be available in the card management update.",fontSize=12.sp,color=muted)
 }
 },confirmButton={TextButton(enabled=name.isNotBlank()&&toPaise(amount)>0,onClick={onSave(FinancePlan(old?.id?:0,kind,name.trim(),toPaise(amount),toPaise(progress),day.toIntOrNull()?.coerceIn(1,28)?:1,months.toIntOrNull()?.coerceAtLeast(1)?:1))}){Text("Save")}},dismissButton={TextButton(onClick=onClose){Text("Cancel")}})
}
