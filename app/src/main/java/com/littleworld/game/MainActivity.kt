package com.littleworld.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

private val forest = Color(0xFF1C3328)
private val paper = Color(0xFFF5F1E7)
private val leaf = Color(0xFF5B805B)
private val gold = Color(0xFFE9BD68)

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  window.statusBarColor = android.graphics.Color.rgb(28,51,40)
  window.navigationBarColor = android.graphics.Color.rgb(28,51,40)
  setContent { MaterialTheme { Game() } }
 }
}
enum class Tool(val label:String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
 SELECT("Select",Icons.Default.TouchApp), HOUSE("House",Icons.Default.Home), ROAD("Road",Icons.Default.Route),
 FARM("Farm",Icons.Default.Grass), PARK("Park",Icons.Default.Park), MARKET("Market",Icons.Default.Storefront)
}
data class Building(val x:Int,val y:Int,val type:Tool)
data class Person(val name:String,val job:String,var x:Float,var y:Float)

@Composable fun Game() {
 val ctx= androidx.compose.ui.platform.LocalContext.current
 val prefs= remember { ctx.getSharedPreferences("littleworld",0) }
 var coins by remember { mutableIntStateOf(prefs.getInt("coins",120)) }
 var day by remember { mutableIntStateOf(prefs.getInt("day",1)) }
 var homes by remember { mutableIntStateOf(prefs.getInt("homes",1)) }
 var people by remember { mutableIntStateOf(prefs.getInt("people",1)) }
 var paused by remember { mutableStateOf(false) }
 var speed by remember { mutableIntStateOf(1) }
 var tool by remember { mutableStateOf(Tool.SELECT) }
 var selected by remember { mutableIntStateOf(0) }
 var message by remember { mutableStateOf("Your first home. A whole world ahead.") }
 val buildings= remember { mutableStateListOf(Building(5,4,Tool.HOUSE)) }
 val residents= remember { mutableStateListOf(Person("Ari","Builder",5.2f,4.2f)) }
 val stage=when { people>=45->"COUNTRY"; people>=20->"CITY"; people>=9->"TOWN"; people>=4->"VILLAGE"; else->"HOMESTEAD" }
 val goal=when(stage) { "HOMESTEAD"->"Welcome 3 more residents"; "VILLAGE"->"Grow to 9 residents"; "TOWN"->"Grow to 20 residents"; "CITY"->"Grow to 45 residents"; else->"Your country is thriving" }
 LaunchedEffect(paused,speed) {
  while(true) {
   delay((1800L/speed).coerceAtLeast(450L))
   if(!paused) {
    day++
    residents.forEach { it.x=(it.x+listOf(-.12f,.10f,.16f,-.08f).random()).coerceIn(1f,10f); it.y=(it.y+listOf(-.1f,.08f,.12f,-.06f).random()).coerceIn(1f,8f) }
    if(day%4==0) coins+=8+people*2
    if(day%7==0 && homes*4>people && stage!="COUNTRY") {
     people++
     residents.add(Person(listOf("Mira","Noah","Ira","Lena","Kai","Ravi","Tara","Milo","Asha","Theo").random(),"Resident",4f+(0..4).random(),3f+(0..3).random()))
     message="A new resident moved in!"
    }
    prefs.edit().putInt("coins",coins).putInt("day",day).putInt("homes",homes).putInt("people",people).apply()
   }
  }
 }
 Box(Modifier.fillMaxSize().background(forest)) {
  Column(Modifier.fillMaxSize().padding(10.dp)) {
   Row(Modifier.fillMaxWidth().padding(bottom=8.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
    Row(verticalAlignment=Alignment.CenterVertically) {
     Surface(color=Color(0xFFDCE9CF),shape=RoundedCornerShape(12.dp)) { Icon(Icons.Default.Public,null,tint=leaf,modifier=Modifier.padding(9.dp).size(21.dp)) }
     Spacer(Modifier.width(9.dp))
     Column { Text("LITTLE WORLD",color=Color.White,fontSize=16.sp,fontWeight=FontWeight.Black,letterSpacing=2.sp); Text("A world shaped by you",color=Color(0xFFB8CDBD),fontSize=10.sp) }
    }
    Row(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalAlignment=Alignment.CenterVertically) {
     Pill("DAY "+day,Icons.Default.WbSunny); Pill(people.toString()+" people",Icons.Default.Groups); Pill(coins.toString(),Icons.Default.Paid)
     Text(stage,color=gold,fontSize=11.sp,fontWeight=FontWeight.Bold)
    }
   }
   Row(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
    Column(Modifier.width(166.dp).fillMaxHeight().clip(RoundedCornerShape(17.dp)).background(paper).padding(11.dp)) {
     Text("YOUR SETTLEMENT",color=leaf,fontSize=9.sp,fontWeight=FontWeight.Black,letterSpacing=1.sp)
     Text(stage.lowercase().replaceFirstChar{it.uppercase()},color=forest,fontSize=22.sp,fontWeight=FontWeight.ExtraBold)
     Text(goal,color=Color(0xFF68756B),fontSize=10.sp)
     Spacer(Modifier.height(13.dp)); Text("BUILD",color=leaf,fontSize=10.sp,fontWeight=FontWeight.Black,letterSpacing=1.sp)
     Tool.values().forEach { t ->
      Surface(onClick={tool=t},color=if(tool==t) Color(0xFFDCE8D4) else Color.Transparent,shape=RoundedCornerShape(9.dp),modifier=Modifier.fillMaxWidth().padding(vertical=2.dp)) {
       Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically) { Icon(t.icon,null,tint=if(tool==t) leaf else Color(0xFF68756B),modifier=Modifier.size(17.dp)); Spacer(Modifier.width(8.dp)); Text(t.label,color=forest,fontSize=11.sp,fontWeight=if(tool==t) FontWeight.Bold else FontWeight.Medium) }
      }
     }
     Spacer(Modifier.weight(1f)); Text(message,color=Color(0xFF68756B),fontSize=9.sp,lineHeight=13.sp)
    }
    Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(17.dp))) {
     World(buildings,residents,selected,tool) { x,y ->
      if(tool==Tool.SELECT) {
       if(residents.isNotEmpty()) { selected=residents.indices.minByOrNull { hypot(residents[it].x-x,residents[it].y-y) } ?: 0; val p=residents[selected]; message=p.name+" • "+p.job }
      } else {
       val cost=when(tool) { Tool.HOUSE->45; Tool.ROAD->5; Tool.FARM->25; Tool.PARK->30; Tool.MARKET->65; else->0 }
       if(coins>=cost) { coins-=cost; buildings.add(Building(x.roundToInt().coerceIn(1,10),y.roundToInt().coerceIn(1,8),tool)); if(tool==Tool.HOUSE) homes++; message=tool.label+" built. Your settlement is growing." }
       else message="Not enough coins. Let the settlement earn more."
      }
     }
     Surface(color=Color(0xCCF5F1E7),shape=RoundedCornerShape(8.dp),modifier=Modifier.align(Alignment.TopStart).padding(10.dp)) { Text("  ISOMETRIC WORLD  ",color=forest,fontSize=9.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(5.dp)) }
     Row(Modifier.align(Alignment.BottomEnd).padding(10.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
      IconButton(onClick={paused=!paused},modifier=Modifier.clip(RoundedCornerShape(9.dp)).background(Color(0xEEF5F1E7))) { Icon(if(paused) Icons.Default.PlayArrow else Icons.Default.Pause,null,tint=forest) }
      IconButton(onClick={speed=if(speed==1) 2 else if(speed==2) 4 else 1; message="Simulation speed: "+speed+"x"},modifier=Modifier.clip(RoundedCornerShape(9.dp)).background(Color(0xEEF5F1E7))) { Icon(Icons.Default.Speed,null,tint=forest) }
     }
    }
    Column(Modifier.width(164.dp).fillMaxHeight().clip(RoundedCornerShape(17.dp)).background(paper).padding(11.dp)) {
     Text("COMMUNITY",color=leaf,fontSize=10.sp,fontWeight=FontWeight.Black,letterSpacing=1.sp)
     Spacer(Modifier.height(8.dp)); Text("Residents",color=Color(0xFF758078),fontSize=10.sp); Text(people.toString(),color=forest,fontSize=27.sp,fontWeight=FontWeight.ExtraBold)
     Spacer(Modifier.height(8.dp)); Text("HOMES",color=Color(0xFF758078),fontSize=9.sp); Text(homes.toString(),color=forest,fontSize=18.sp,fontWeight=FontWeight.Bold)
     Divider(Modifier.padding(vertical=8.dp),color=Color(0xFFDDE1D8))
     Text("PEOPLE",color=leaf,fontSize=10.sp,fontWeight=FontWeight.Black,letterSpacing=1.sp)
     residents.take(8).forEachIndexed { i,p ->
      Surface(onClick={selected=i;message=p.name+" selected"},color=if(selected==i) Color(0xFFDCE8D4) else Color.Transparent,shape=RoundedCornerShape(8.dp),modifier=Modifier.fillMaxWidth().padding(vertical=2.dp)) {
       Row(Modifier.padding(5.dp),verticalAlignment=Alignment.CenterVertically) { Surface(color=Color(0xFFE7C89C),shape=RoundedCornerShape(7.dp)) { Text(p.name.take(1),color=forest,fontSize=10.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(6.dp)) }; Spacer(Modifier.width(6.dp)); Column { Text(p.name,color=forest,fontSize=10.sp,fontWeight=FontWeight.Bold); Text(p.job,color=Color(0xFF758078),fontSize=8.sp) } }
      }
     }
     Spacer(Modifier.weight(1f)); Text("Select a resident to follow them.",color=Color(0xFF758078),fontSize=9.sp)
    }
   }
   Text("Tap land to build  •  Select residents to inspect  •  Your world keeps living",color=Color(0xFFB8CDBD),fontSize=9.sp,modifier=Modifier.align(Alignment.CenterHorizontally).padding(top=6.dp))
  }
 }
}

@Composable fun Pill(label:String,icon:androidx.compose.ui.graphics.vector.ImageVector) {
 Surface(color=Color(0xFF30483B),shape=RoundedCornerShape(9.dp)) { Row(Modifier.padding(horizontal=8.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically) { Icon(icon,null,tint=gold,modifier=Modifier.size(12.dp)); Spacer(Modifier.width(4.dp)); Text(label,color=Color.White,fontSize=9.sp,fontWeight=FontWeight.SemiBold) } }
}

@Composable fun World(buildings:List<Building>,people:List<Person>,selected:Int,tool:Tool,onTap:(Float,Float)->Unit) {
 Canvas(Modifier.fillMaxSize().pointerInput(tool) { detectTapGestures { p ->
  val tw=size.width/15f; val th=tw*.5f; val dx=p.x-size.width*.51f; val dy=p.y-size.height*.47f
  onTap((dx/tw+dy/th+7.5f)/2f,(dy/th-dx/tw+7.5f)/2f)
 } }) {
  val tw=size.width/15f; val th=tw*.5f; val cx=size.width*.51f; val cy=size.height*.47f
  fun iso(x:Float,y:Float,z:Float=0f)=Offset(cx+(x-y)*tw/2f,cy+(x+y-8f)*th/2f-z)
  drawRect(Brush.verticalGradient(listOf(Color(0xFFB9D4C3),Color(0xFF82A98D),Color(0xFF6D9679))))
  for(y in 0..9) for(x in 0..11) {
   val c=iso(x.toFloat(),y.toFloat()); val path=Path().apply { moveTo(c.x,c.y); lineTo(c.x+tw/2,c.y+th/2); lineTo(c.x,c.y+th); lineTo(c.x-tw/2,c.y+th/2); close() }
   drawPath(path,if((x+y)%2==0) Color(0xFF9FC58E) else Color(0xFF96BC86)); drawPath(path,Color(0x22617F5B),style=Stroke(1.dp.toPx()))
  }
  for(y in 0..9) { val c=iso(11f,y.toFloat()); drawOval(Color(0xFF72B7C3),Offset(c.x-tw*.24f,c.y),Size(tw*.48f,th*.5f)) }
  buildings.sortedBy{it.x+it.y}.forEach { b ->
   val c=iso(b.x.toFloat(),b.y.toFloat()); val h=tw*.48f
   when(b.type) {
    Tool.HOUSE -> {
     val l=Path().apply { moveTo(c.x-tw*.25f,c.y); lineTo(c.x,c.y+th*.25f); lineTo(c.x,c.y+h+th*.25f); lineTo(c.x-tw*.25f,c.y+h); close() }
     val r=Path().apply { moveTo(c.x,c.y+th*.25f); lineTo(c.x+tw*.25f,c.y); lineTo(c.x+tw*.25f,c.y+h); lineTo(c.x,c.y+h+th*.25f); close() }
     drawPath(l,Color(0xFFE9D7B8)); drawPath(r,Color(0xFFC9B18E))
     val roof=Path().apply { moveTo(c.x-tw*.32f,c.y); lineTo(c.x,c.y-h*.35f); lineTo(c.x+tw*.32f,c.y); lineTo(c.x,c.y+th*.34f); close() }
     drawPath(roof,Color(0xFFB86B4F)); drawRect(Color(0xFF7C9AA1),Offset(c.x-tw*.17f,c.y+h*.35f),Size(tw*.07f,h*.18f)); drawRect(Color(0xFF8C674B),Offset(c.x-tw*.035f,c.y+h*.48f),Size(tw*.08f,h*.28f))
    }
    Tool.MARKET -> { drawRect(Color(0xFFE6D6B9),Offset(c.x-tw*.22f,c.y+h*.18f),Size(tw*.44f,h*.55f)); drawRect(Color(0xFF9B6C4E),Offset(c.x-tw*.27f,c.y-h*.02f),Size(tw*.54f,h*.16f)); drawRect(Color(0xFFE6B65D),Offset(c.x-tw*.25f,c.y-h*.12f),Size(tw*.5f,h*.17f)) }
    Tool.FARM -> { drawOval(Color(0xFF6B9B52),Offset(c.x-tw*.28f,c.y),Size(tw*.58f,th*.55f)); for(i in 0..3) drawLine(Color(0xFFE5C978),Offset(c.x-tw*.2f+i*tw*.1f,c.y+th*.12f),Offset(c.x-tw*.2f+i*tw*.1f,c.y+th*.43f),2.dp.toPx()) }
    Tool.PARK -> { drawCircle(Color(0xFF477A4E),tw*.18f,Offset(c.x,c.y-th*.1f)); drawCircle(Color(0xFF83B86A),tw*.14f,Offset(c.x-tw*.07f,c.y-th*.2f)); drawLine(Color(0xFF795C3B),Offset(c.x,c.y-th*.1f),Offset(c.x,c.y+th*.4f),3.dp.toPx()) }
    Tool.ROAD -> { val p=Path().apply { moveTo(c.x-tw*.25f,c.y+th*.25f); lineTo(c.x,c.y+th*.5f); lineTo(c.x+tw*.25f,c.y+th*.25f); lineTo(c.x,c.y); close() }; drawPath(p,Color(0xFFB8A88A)) }
    else -> {}
   }
  }
  listOf(1 to 1,3 to 7,8 to 2,9 to 7,2 to 9,7 to 8).forEach { (x,y) ->
   val c=iso(x.toFloat(),y.toFloat()); drawCircle(Color(0xFF477A4E),tw*.16f,Offset(c.x,c.y-th*.18f)); drawCircle(Color(0xFF78A95D),tw*.13f,Offset(c.x-tw*.07f,c.y-th*.24f)); drawLine(Color(0xFF76583B),Offset(c.x,c.y-th*.08f),Offset(c.x,c.y+th*.35f),3.dp.toPx())
  }
  people.forEachIndexed { i,p ->
   val c=iso(p.x,p.y); drawOval(Color(0x330E251B),Offset(c.x-tw*.09f,c.y+th*.12f),Size(tw*.2f,th*.12f))
   if(i==selected) drawCircle(gold,tw*.17f,Offset(c.x,c.y-th*.16f),style=Stroke(3.dp.toPx()))
   drawLine(if(i%2==0) Color(0xFF476C9C) else Color(0xFFB56C52),Offset(c.x,c.y-th*.25f),Offset(c.x,c.y+th*.1f),5.dp.toPx(),cap=StrokeCap.Round)
   drawCircle(Color(0xFFF0C59A),tw*.075f,Offset(c.x,c.y-th*.35f))
  }
 }
}