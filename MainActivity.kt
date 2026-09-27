package com.ganatalks.personalloanofficer

import android.app.*
import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

data class RateRow(val min: Long, val max: Long, val salNew: Double?, val salPltb: Double?, val sempNew: Double?, val sempPltb: Double?)

private val defaultRates = listOf(
    RateRow(40000,149999,29.99,29.99,29.99,29.99),
    RateRow(150000,229999,29.99,29.99,29.99,29.99),
    RateRow(230000,299999,29.99,29.99,29.99,29.99),
    RateRow(300000,334999,27.75,26.50,28.00,26.99),
    RateRow(335000,399999,24.75,23.25,25.25,23.75),
    RateRow(400000,449999,23.75,22.25,24.25,22.75),
    RateRow(450000,499999,22.25,21.75,23.25,22.50),
    RateRow(500000,549999,21.75,20.75,22.25,21.25),
    RateRow(550000,574999,21.25,20.75,21.75,21.25),
    RateRow(575000,624999,20.75,20.25,21.25,20.75),
    RateRow(625000,649999,20.00,19.75,20.25,null),
    RateRow(650000,699999,19.50,19.50,20.00,null),
    RateRow(700000,749999,19.25,19.25,19.75,null),
    RateRow(750000,799999,19.25,19.25,19.75,null),
    RateRow(800000,1150000,19.25,19.25,19.75,null)
)

fun money(v: Double): String =
    NumberFormat.getNumberInstance(Locale("en","IN")).apply { minimumFractionDigits=2; maximumFractionDigits=2 }.format(v).let { "₹$it" }

fun lookupRate(amount: Long, type: String, category: String): Double? {
    val row = defaultRates.firstOrNull { amount in it.min..it.max } ?: return null
    return when (type to category) {
        "Salaried" to "New" -> row.salNew
        "Salaried" to "PLTB" -> row.salPltb
        "Self Employed" to "New" -> row.sempNew
        "Self Employed" to "PLTB" -> row.sempPltb
        else -> null
    }
}

fun emi(principal: Double, annual: Double, months: Int): Double {
    if (months <= 0) return 0.0
    val r = annual / 12.0 / 100.0
    if (r == 0.0) return principal / months
    return principal * r * (1+r).pow(months) / ((1+r).pow(months)-1)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    var loggedIn by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    if (!loggedIn) LoginScreen(name, mobile, { name=it }, { mobile=it }) { loggedIn=true }
    else LoanApp(name)
}

@Composable
fun LoginScreen(name:String,mobile:String,setName:(String)->Unit,setMobile:(String)->Unit,onLogin:()->Unit) {
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.padding(24.dp), verticalArrangement=Arrangement.Center) {
            Text("PERSONAL LOAN OFFICER", style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(8.dp)); Text("Welcome")
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(name,setName,Modifier.fillMaxWidth(),label={Text("Executive Name")})
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(mobile,setMobile,Modifier.fillMaxWidth(),label={Text("Mobile Number")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Phone))
            Spacer(Modifier.height(20.dp))
            Button(onClick={if(name.isNotBlank() && mobile.length==10) onLogin()},Modifier.fillMaxWidth()) { Text("LOGIN") }
        }
    }
}

@Composable
fun LoanApp(executive:String) {
    var tab by remember { mutableStateOf(0) }
    Scaffold(bottomBar={
        NavigationBar {
            listOf("HOME","CALCULATOR","CALLBACKS","SETTINGS").forEachIndexed { i,label ->
                NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(if(i==0) Icons.Default.Home else if(i==1) Icons.Default.Calculate else if(i==2) Icons.Default.Notifications else Icons.Default.Settings,label)},label={Text(label)})
            }
        }
    }) { p ->
        Box(Modifier.padding(p)) {
            when(tab) {
                0 -> HomeScreen(executive,{tab=1},{tab=2})
                1 -> CalculatorScreen()
                2 -> CallbackScreen()
                else -> SettingsScreen(executive)
            }
        }
    }
}

@Composable
fun HomeScreen(executive:String,calc:()->Unit,callback:()->Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item {
            Text("PERSONAL LOAN OFFICER",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
            Text("Welcome, $executive")
        }
        item { Button(calc,Modifier.fillMaxWidth().height(70.dp)){Text("NORMAL EMI CALCULATOR")} }
        item { Button(callback,Modifier.fillMaxWidth().height(70.dp)){Text("CUSTOMER REQUEST SEARCH")} }
        item { OutlinedButton(callback,Modifier.fillMaxWidth().height(70.dp)){Text("CALL BACK RECORDS")} }
    }
}

@Composable
fun CalculatorScreen() {
    var amount by remember { mutableStateOf("880000") }
    var tenure by remember { mutableStateOf("48") }
    var type by remember { mutableStateOf("Salaried") }
    var category by remember { mutableStateOf("PLTB") }
    var insuranceMode by remember { mutableStateOf("DEFAULT INSURANCE") }
    var manual by remember { mutableStateOf(false) }
    var manualRate by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Double?>(null) }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Text("NORMAL EMI CALCULATOR",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold) }
        item { OutlinedTextField(amount,{amount=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("Disbursement Amount")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number)) }
        item { OutlinedTextField(tenure,{tenure=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("Tenure (Months)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number)) }
        item { Text("Customer Type") }
        item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ Button({type="Salaried"}){Text("Salaried")} ; OutlinedButton({type="Self Employed"}){Text("Self Employed")} } }
        item { Text("Customer Category") }
        item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ Button({category="New"}){Text("New")} ; OutlinedButton({category="PLTB"}){Text("PLTB")} } }
        item { Row(verticalAlignment=Alignment.CenterVertically){Checkbox(manual,{manual=it});Text("Manual Rate")} }
        if(manual) item { OutlinedTextField(manualRate,{manualRate=it},Modifier.fillMaxWidth(),label={Text("Annual Reducing Rate %")}) }
        item { Text("Insurance") }
        item { Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ OutlinedButton({insuranceMode="WITHOUT INSURANCE"}){Text("Without")} ; OutlinedButton({insuranceMode="DEFAULT INSURANCE"}){Text("Default")} ; OutlinedButton({insuranceMode="CUSTOM INSURANCE"}){Text("Custom")} } }
        item { Button({result=calculate(amount,tenure,type,category,manual,manualRate,insuranceMode)},Modifier.fillMaxWidth().height(58.dp)){Text("CALCULATE EMI")} }
        result?.let { item { ResultCard(amount,tenure,type,category,manual,manualRate,insuranceMode,it) } }
    }
}

fun calculate(amountS:String,tenureS:String,type:String,cat:String,manual:Boolean,manualRate:String,ins:String):Double {
    val amount=amountS.toLongOrNull()?:0
    val fee=amount*0.0413
    val insurance=when(ins) {
        "WITHOUT INSURANCE" -> 0.0
        "CUSTOM INSURANCE" -> 0.0
        else -> if(amount < 500000) 20000.0 else 24500.0
    }
    val principal=amount+fee+insurance
    val rate=if(manual) manualRate.toDoubleOrNull()?:0.0 else lookupRate(amount,type,cat)?:0.0
    return emi(principal,rate,tenureS.toIntOrNull()?:36)
}

@Composable
fun ResultCard(amountS:String,tenureS:String,type:String,cat:String,manual:Boolean,manualRate:String,ins:String,emiValue:Double) {
    val amount=amountS.toLongOrNull()?:0
    val fee=amount*0.0413
    val insurance=when(ins) {"WITHOUT INSURANCE"->0.0 else if(amount<500000)20000.0 else 24500.0}
    val principal=amount+fee+insurance
    val rate=if(manual)manualRate.toDoubleOrNull()?:0.0 else lookupRate(amount,type,cat)
    val months=tenureS.toIntOrNull()?:36
    val total=emiValue*months
    val interest=total-principal
    val effectiveAnnual=if(months>0 && amount>0) interest/amount/(months/12.0)*100 else 0.0
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("LOAN CALCULATION",fontWeight=FontWeight.Bold)
            Text("Disbursement Amount: ${money(amount.toDouble())}")
            Text("Processing Fee: ${money(fee)}")
            Text("Insurance: ${money(insurance)}")
            Text("Total Financed Amount: ${money(principal)}")
            Text("Tenure: $months Months")
            Text("Annual Reducing Interest Rate: ${rate?.let{"%.2f".format(it)} ?: "NOT AVAILABLE"}%")
            Text("Monthly Reducing Interest Rate: ${rate?.let{"%.2f".format(it/12)} ?: "NOT AVAILABLE"}%")
            Text("EFFECTIVE ANNUAL RATE: ${"%.2f".format(effectiveAnnual)}%",fontWeight=FontWeight.Bold)
            Text("EFFECTIVE MONTHLY RATE: ${"%.2f".format(effectiveAnnual/12)}%",fontWeight=FontWeight.Bold)
            Text("MONTHLY EMI: ${money(emiValue)}",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
            Text("Total Interest Paid: ${money(interest)}")
            Text("Total Amount Paid: ${money(total)}")
        }
    }
}

@Composable
fun CallbackScreen() {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text("CALL BACK RECORDS",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
        OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text("Customer Name")})
        OutlinedTextField(number,{number=it},Modifier.fillMaxWidth(),label={Text("Customer Mobile Number")})
        OutlinedTextField(limit,{limit=it},Modifier.fillMaxWidth(),label={Text("Loan Amount / Loan Limit")})
        OutlinedTextField(reason,{reason=it},Modifier.fillMaxWidth(),label={Text("Reason")})
        Button({ },Modifier.fillMaxWidth()){Text("SAVE CALLBACK")}
        Text("Room database and exact alarm scheduling are scaffolded in the project and are the next implementation layer.")
    }
}

@Composable
fun SettingsScreen(executive:String) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Text("SETTINGS",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold) }
        item { Text("Executive Profile") }
        item { Text(executive) }
        item { Text("Branch: Somajiguda, Hyderabad") }
        item { Text("Processing Fee: 4.13%") }
        item { Text("Insurance: ₹20,000 below ₹5,00,000; ₹24,500 at/above ₹5,00,000") }
        item { Text("Rate Data: Default rate table included") }
        item { Text("Backup & Restore: Local-only architecture") }
    }
}
