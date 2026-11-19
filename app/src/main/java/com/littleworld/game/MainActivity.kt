package com.littleworld.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

private val Forest = Color(0xFF1D352A)
private val Paper = Color(0xFFF5F1E7)
private val Leaf = Color(0xFF5B805B)
private val Gold = Color(0xFFE9BD68)

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  window.statusBarColor = android.graphics.Color.rgb(29,53,42)
  window.navigationBarColor = android.graphics.Color.rgb(29,53,42)
  setContent { MaterialTheme { LittleWorld() } }
 }
}

enum class BuildTool(val title:String,val icon:androidx.compose.ui.graphics.vector.ImageVector,val cost:Int) {
 SELECT("Inspect",Icons.Default.TouchApp,0), HOUSE("Home",Icons.Default.Home,45),
 ROAD("Road",Icons.Default.Route,5), FARM("Farm",Icons.Default.Grass,25),
 PARK("Park",Icons.Default.Park,30), MARKET("Market",Icons.Default.Storefront,65),
 WELL("Well",Icons.Default.WaterDrop,35), WORKSHOP("Workshop",Icons.Default.Build,80),
 SCHOOL("School",Icons.Default.School,110), CLINIC("Clinic",Icons.Default.LocalHospital,125)
}
enum class Activity(val label:String) { IDLE("Wandering"), WORK("Working"), EAT("Eating"), REST("Resting"), SOCIAL("Socialising"), FOLLOW("Following you") }
data class Structure(val x:Int,val y:Int,val kind:BuildTool)
data class Citizen(var name:String,var job:String,var x:Float,var y:Float,var hunger:Float=.9f,var energy:Float=.9f,var happiness:Float=.8f,var action:Activity=Activity.IDLE,var targetX:Float=x,var targetY:Float=y)

@Composable fun LittleWorld() {
 val ctx=androidx.compose.ui.platform.LocalContext.current
 val prefs= remember { ctx.getSharedPreferences("littleworld-save",0) }
 var coins by remember { mutableIntStateOf(prefs.getInt("coins",160)) }
 var day by remember { mutableIntStateOf(prefs.getInt("day",1)) }
 var paused by remember { mutableStateOf(false) }
 var speed by remember { mutableIntStateOf(1) }
 var tool by remember { mutableStateOf(BuildTool.SELECT) }
 var selected by remember { mutableIntStateOf(0) }
 var message by remember { mutableStateOf("A quiet patch of land. Your story starts here.") }
 val buildings= remember { mutableStateListOf(Structure(11,8,BuildTool.HOUSE),Structure(8,9,BuildTool.WELL)) }
 val citizens= remember { mutableStateListOf(Citizen("Ari","Founder",11.2f,8.3f)) }
 val homes by remember { derivedStateOf { buildings.count { it.kind==BuildTool.HOUSE } } }
 val population=citizens.size
 val stage=when { population>=45->"COUNTRY"; population>=20->"CITY"; population>=9->"TOWN"; population>=4->"VILLAGE"; else->"HOMESTEAD" }
 val goal=when(stage) { "HOMESTEAD"->"Build homes and welcome 3 more people"; "VILLAGE"->"Create a town with 9 residents"; "TOWN"->"Reach 20 residents to become a city"; "CITY"->"Reach 45 residents to found a country"; else->"Your country is growing. Keep building!" }
 LaunchedEffect(paused,speed) {
  while(true) {
   delay((1700L/speed).coerceAtLeast(350L))
   if(!paused) {
    day++
    citizens.forEachIndexed { i,p ->
     when(p.action) {
      Activity.WORK -> { coins+=1; p.energy=(p.energy-.012f).coerceAtLeast(0f) }
      Activity.EAT -> p.hunger=(p.hunger+.18f).coerceAtMost(1f)
      Activity.REST -> p.energy=(p.energy+.16f).coerceAtMost(1f)
      Activity.SOCIAL -> p.happiness=(p.happiness+.12f).coerceAtMost(1f)
      else -> { val dx=p.targetX-p.x; val dy=p.targetY-p.y; val distance=hypot(dx,dy); if(distance<.22f){p.targetX=(2..24).random().toFloat();p.targetY=(2..24).random().toFloat()} else {val step=.16f;p.x=(p.x+dx/distance*step).coerceIn(1f,28f);p.y=(p.y+dy/distance*step).coerceIn(1f,28f)} }
     }
     if(p.action!=Activity.EAT) p.hunger=(p.hunger-.006f).coerceAtLeast(0f)
     if(p.action!=Activity.REST && p.action!=Activity.WORK) p.energy=(p.energy-.004f).coerceAtLeast(0f)
     if(p.hunger<.25f || p.energy<.2f) p.happiness=(p.happiness-.008f).coerceAtLeast(0f)
     if(p.hunger>.7f && p.energy>.55f) p.happiness=(p.happiness+.003f).coerceAtMost(1f)
    }
    if(day%4==0) coins+=3+population*2
    if(day%6==0 && homes*4>population && population<100) {
     val name=listOf("Mira","Noah","Ira","Lena","Kai","Ravi","Tara","Milo","Asha","Theo","Nia","Arun","Sana","Leo").filter { n->citizens.none{it.name==n} }.randomOrNull() ?: "Resident ${population+1}"
     citizens.add(Citizen(name,"New resident",8f+(0..7).random(),7f+(0..5).random()))
     message="$name moved into your settlement."
    }
    prefs.edit().putInt("coins",coins).putInt("day",day).apply()
   }
  }
 }
 Box(Modifier.fillMaxSize().background(Color(0xFF172B23))) {
  Column(Modifier.fillMaxSize().padding(horizontal=14.dp, vertical=12.dp)) {
   Row(Modifier.fillMaxWidth().padding(bottom=8.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
    Row(verticalAlignment=Alignment.CenterVertically) {
     Surface(color=Color(0xFFDCE9CF),shape=RoundedCornerShape(12.dp)) { Icon(Icons.Default.Public,null,tint=Leaf,modifier=Modifier.padding(9.dp).size(22.dp)) }
     Spacer(Modifier.width(9.dp)); Column { Text("LITTLE WORLD",color=Color.White,fontSize=16.sp,fontWeight=FontWeight.Black,letterSpacing=2.sp); Text("Build a life. Shape a civilisation.",color=Color(0xFFB8CDBD),fontSize=10.sp) }
    }
    Row(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalAlignment=Alignment.CenterVertically) {
     Pill("DAY $day",Icons.Default.WbSunny); Pill("$population people",Icons.Default.Groups); Pill("$coins",Icons.Default.Paid)
     Text(stage,color=Gold,fontSize=11.sp,fontWeight=FontWeight.Bold)
    }
   }
   Row(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
    Column(Modifier.width(174.dp).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(Brush.verticalGradient(listOf(Color(0xFFFAF8F0),Color(0xFFE9EBDD)))).padding(12.dp)) {
     Text("SETTLEMENT",color=Leaf,fontSize=9.sp,fontWeight=FontWeight.Black,letterSpacing=1.sp)
     Text(stage.lowercase().replaceFirstChar{it.uppercase()},color=Forest,fontSize=21.sp,fontWeight=FontWeight.ExtraBold)
     Text(goal,color=Color(0xFF68756B),fontSize=9.sp,lineHeight=12.sp)
     Spacer(Modifier.height(9.dp)); Text("CONSTRUCTION",color=Leaf,fontSize=9.sp,fontWeight=FontWeight.Black,letterSpacing=1.sp)
     Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(1.dp)) {
      BuildTool.values().forEach { t ->
       Surface(onClick={tool=t},color=if(tool==t) Color(0xFFDCE8D4) else Color.Transparent,shape=RoundedCornerShape(8.dp),modifier=Modifier.fillMaxWidth()) {
         Row(Modifier.padding(horizontal=6.dp,vertical=5.dp),verticalAlignment=Alignment.CenterVertically) { Icon(t.icon,null,tint=if(tool==t) Leaf else Color(0xFF68756B),modifier=Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(t.title,color=Forest,fontSize=10.sp,fontWeight=if(tool==t) FontWeight.Bold else FontWeight.Medium); Spacer(Modifier.weight(1f)); if(t.cost>0) Text("${'$'}${t.cost}",color=Color(0xFF798278),fontSize=8.sp) }
       }
      }
     }
     Text(message,color=Color(0xFF68756B),fontSize=8.sp,lineHeight=11.sp)
    }
    Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(Color(0xFF8FB88C))) {
     IsometricMap(buildings,citizens,selected,tool,onTap={x,y->
      if(tool==BuildTool.SELECT) {
       if(citizens.isNotEmpty()) { selected=citizens.indices.minByOrNull { hypot(citizens[it].x-x,citizens[it].y-y) } ?: 0; message="${citizens[selected].name} • ${citizens[selected].job}" }
      } else if(coins>=tool.cost) {
       val xx=x.roundToInt().coerceIn(1,22); val yy=y.roundToInt().coerceIn(1,16)
       buildings.add(Structure(xx,yy,tool)); coins-=tool.cost
       if(tool==BuildTool.HOUSE) message="A new home is ready. More residents may arrive."
       else message="${tool.title} built."
      } else message="Not enough coins for a ${tool.title.lowercase()}."
     })
     Surface(color=Color(0xCCF5F1E7),shape=RoundedCornerShape(8.dp),modifier=Modifier.align(Alignment.TopStart).padding(9.dp)) { Text("  ISOMETRIC WORLD  •  DRAG TO MOVE  •  PINCH TO ZOOM  ",color=Forest,fontSize=8.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(5.dp)) }
     Row(Modifier.align(Alignment.BottomEnd).padding(8.dp),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
      MiniButton(if(paused) Icons.Default.PlayArrow else Icons.Default.Pause){paused=!paused}
      MiniButton(Icons.Default.Speed){speed=if(speed==1) 2 else if(speed==2) 4 else 1;message="Simulation speed: ${speed}x"}
     }
    }
    Column(Modifier.width(198.dp).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(Brush.verticalGradient(listOf(Color(0xFFFAF8F0),Color(0xFFE9EBDD)))).padding(12.dp)) {
     Text("PEOPLE & LIFE",color=Leaf,fontSize=10.sp,fontWeight=FontWeight.Black,letterSpacing=1.sp)
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { MiniStat("PEOPLE",population.toString()); MiniStat("HOMES",homes.toString()); MiniStat("JOBS",buildings.count{it.kind==BuildTool.WORKSHOP}.toString()) }
     Divider(Modifier.padding(vertical=7.dp),color=Color(0xFFDDE1D8))
     Text("RESIDENTS",color=Leaf,fontSize=9.sp,fontWeight=FontWeight.Black)
     Column(Modifier.weight(1f)) {
      citizens.take(12).forEachIndexed { i,p ->
       Surface(onClick={selected=i;message="${p.name} selected"},color=if(selected==i) Color(0xFFDCE8D4) else Color.Transparent,shape=RoundedCornerShape(7.dp),modifier=Modifier.fillMaxWidth().padding(vertical=1.dp)) {
        Row(Modifier.padding(5.dp),verticalAlignment=Alignment.CenterVertically) {
         Surface(color=Color(0xFFE7C89C),shape=RoundedCornerShape(7.dp)){Text(p.name.take(1),color=Forest,fontSize=9.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(5.dp))}
         Spacer(Modifier.width(5.dp)); Column(Modifier.weight(1f)) { Text(p.name,color=Forest,fontSize=9.sp,fontWeight=FontWeight.Bold); Text(p.action.label,color=Color(0xFF758078),fontSize=7.sp) }
        }
       }
      }
     }
     Divider(Modifier.padding(vertical=5.dp),color=Color(0xFFDDE1D8))
     if(citizens.isNotEmpty()) {
      val p=citizens[selected.coerceIn(0,citizens.lastIndex)]
      Text("CONTROL: ${p.name.uppercase()}",color=Leaf,fontSize=8.sp,fontWeight=FontWeight.Black)
      NeedBar("Hunger",p.hunger); NeedBar("Energy",p.energy); NeedBar("Mood",p.happiness)
      Row(horizontalArrangement=Arrangement.spacedBy(4.dp),modifier=Modifier.fillMaxWidth()) {
       ActionButton("Work",Icons.Default.Work){p.action=Activity.WORK}
       ActionButton("Eat",Icons.Default.Restaurant){p.action=Activity.EAT}
       ActionButton("Rest",Icons.Default.Bedtime){p.action=Activity.REST}
      }
      Row(horizontalArrangement=Arrangement.spacedBy(4.dp),modifier=Modifier.fillMaxWidth()) {
       ActionButton("Social",Icons.Default.Favorite){p.action=Activity.SOCIAL}
       ActionButton("Wander",Icons.Default.DirectionsWalk){p.action=Activity.IDLE}
      }
     }
    }
   }
   Text("Select a resident to control their routine  •  Build homes to attract new families  •  Save is automatic",color=Color(0xFFB8CDBD),fontSize=8.sp,modifier=Modifier.align(Alignment.CenterHorizontally).padding(top=5.dp))
  }
 }
}

@Composable fun Pill(label:String,icon:androidx.compose.ui.graphics.vector.ImageVector) {
 Surface(color=Color(0xFF30483B),shape=RoundedCornerShape(9.dp)) { Row(Modifier.padding(horizontal=8.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically) { Icon(icon,null,tint=Gold,modifier=Modifier.size(12.dp)); Spacer(Modifier.width(4.dp)); Text(label,color=Color.White,fontSize=9.sp,fontWeight=FontWeight.SemiBold) } }
}
@Composable fun MiniStat(label:String,value:String) { Column { Text(label,color=Color(0xFF758078),fontSize=7.sp); Text(value,color=Forest,fontSize=16.sp,fontWeight=FontWeight.Bold) } }
@Composable fun NeedBar(label:String,value:Float) {
 Column(Modifier.padding(top=3.dp)) { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label,color=Color(0xFF758078),fontSize=7.sp);Text("${(value*100).roundToInt()}%",color=Forest,fontSize=7.sp)}; LinearProgressIndicator(progress={value.coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(3.dp)),color=if(value<.25f) Color(0xFFC66A55) else Leaf,trackColor=Color(0xFFE1E4DA)) }
}
@Composable fun ActionButton(label:String,icon:androidx.compose.ui.graphics.vector.ImageVector,action:()->Unit) {
 Surface(onClick=action,color=Color(0xFFE6EBDD),shape=RoundedCornerShape(7.dp),modifier=Modifier.width(48.dp)) { Column(Modifier.padding(vertical=5.dp),horizontalAlignment=Alignment.CenterHorizontally) { Icon(icon,null,tint=Leaf,modifier=Modifier.size(14.dp)); Text(label,color=Forest,fontSize=7.sp,fontWeight=FontWeight.SemiBold) } }
}
@Composable fun MiniButton(icon:androidx.compose.ui.graphics.vector.ImageVector,action:()->Unit) {
 Surface(onClick=action,color=Color(0xEEF5F1E7),shape=RoundedCornerShape(9.dp)){Box(Modifier.size(32.dp),contentAlignment=Alignment.Center){Icon(icon,null,tint=Forest,modifier=Modifier.size(17.dp))}}
}

@Composable fun IsometricMap(buildings:List<Structure>,people:List<Citizen>,selected:Int,tool:BuildTool,onTap:(Float,Float)->Unit) {
 var zoom by remember { mutableFloatStateOf(.9f) }
 var pan by remember { mutableStateOf(Offset.Zero) }
 val walk by rememberInfiniteTransition(label="resident-walk").animateFloat(initialValue=0f,targetValue=(2f*PI.toFloat()),animationSpec=infiniteRepeatable(tween(520,easing=LinearEasing)),label="walk-cycle")
 Canvas(Modifier.fillMaxSize()
  .pointerInput(Unit) { detectTransformGestures { _,delta,z,_ -> zoom=(zoom*z).coerceIn(.55f,2.5f); pan+=delta } }
  .pointerInput(tool,zoom,pan) { detectTapGestures { point ->
   val center=Offset(size.width/2f,size.height/2f)
   val unscaled=Offset((point.x-center.x-pan.x)/zoom+center.x,(point.y-center.y-pan.y)/zoom+center.y)
   val tw=size.width/26f; val th=tw*.5f; val dx=unscaled.x-size.width*.51f; val dy=unscaled.y-size.height*.48f
   onTap((dx/tw+dy/th+15f)/2f,(dy/th-dx/tw+15f)/2f)
  } }
 ) {
  drawRect(Brush.verticalGradient(listOf(Color(0xFFB9D8C5),Color(0xFF92BC91),Color(0xFF648D70))))
  // Soft distant woodland and atmospheric horizon
  for(i in 0..18){val x=size.width*i/18f;val h=(18+(i*17%31)).dp.toPx();drawCircle(Color(0x44709B76),h,Offset(x,size.height*.13f))}
  val center=Offset(size.width/2f,size.height/2f)
  withTransform({ translate(pan.x,pan.y); scale(zoom,zoom,center) }) {
   val tw=size.width/26f; val th=tw*.5f; val cx=size.width*.51f; val cy=size.height*.48f
   fun iso(x:Float,y:Float)=Offset(cx+(x-y)*tw/2f,cy+(x+y-15f)*th/2f)
   // 30 x 30 map with varied grass tones and subtle tile edges
   for(y in 0..29) for(x in 0..29) {
    val c=iso(x.toFloat(),y.toFloat())
    val path=Path().apply { moveTo(c.x,c.y);lineTo(c.x+tw/2,c.y+th/2);lineTo(c.x,c.y+th);lineTo(c.x-tw/2,c.y+th/2);close() }
    val edge=(x in 0..1 || y in 0..1 || x>=28 || y>=28)
    drawPath(path,when { edge->Color(0xFF82A87D);(x*7+y*3)%11==0->Color(0xFFA9C995);(x+y)%2==0->Color(0xFF9FC58E);else->Color(0xFF96BC86) })
    drawPath(path,Color(0x1E55734F),style=Stroke(.7.dp.toPx()))
   }
   // creek and crossing
   for(y in 0..29) { val riverX=26.5f+sin(y*.31f)*1.1f; val c=iso(riverX,y.toFloat());drawOval(Color(0xFF4D9DAA),Offset(c.x-tw*.36f,c.y-th*.08f),Size(tw*.72f,th*.82f));drawOval(Color(0xFF83C7D0),Offset(c.x-tw*.22f,c.y+th*.04f),Size(tw*.44f,th*.48f));drawLine(Color(0x88D9F2E9),Offset(c.x-tw*.12f,c.y+th*.22f),Offset(c.x+tw*.12f,c.y+th*.18f),1.dp.toPx()) }
   for(y in 0..29) for(x in 0..29) if((x*13+y*7)%37==0 && x>2 && y>2) {
    val c=iso(x.toFloat(),y.toFloat()); val trunk=Offset(c.x,c.y+th*.24f);drawOval(Color(0x330E2818),Offset(c.x-tw*.19f,c.y+th*.27f),Size(tw*.38f,th*.15f));drawLine(Color(0xFF765039),Offset(c.x,c.y-th*.02f),trunk,tw*.075f,cap=StrokeCap.Round);drawCircle(Color(0xFF376D45),tw*.20f,Offset(c.x,c.y-th*.20f));drawCircle(Color(0xFF4E8952),tw*.17f,Offset(c.x-tw*.10f,c.y-th*.27f));drawCircle(Color(0xFF76A95F),tw*.13f,Offset(c.x+tw*.08f,c.y-th*.31f));drawCircle(Color(0xFF8DBD6D),tw*.075f,Offset(c.x-tw*.07f,c.y-th*.39f))
   }
   buildings.sortedBy{it.x+it.y}.forEach { b -> val c=iso(b.x.toFloat(),b.y.toFloat()); val h=tw*.48f
    when(b.kind) {
     BuildTool.HOUSE -> {
      val left=Path().apply{moveTo(c.x-tw*.25f,c.y);lineTo(c.x,c.y+th*.25f);lineTo(c.x,c.y+h+th*.25f);lineTo(c.x-tw*.25f,c.y+h);close()}
      val right=Path().apply{moveTo(c.x,c.y+th*.25f);lineTo(c.x+tw*.25f,c.y);lineTo(c.x+tw*.25f,c.y+h);lineTo(c.x,c.y+h+th*.25f);close()}
      drawPath(left,Color(0xFFE9D7B8));drawPath(right,Color(0xFFC9B18E))
      val roof=Path().apply{moveTo(c.x-tw*.32f,c.y);lineTo(c.x,c.y-h*.35f);lineTo(c.x+tw*.32f,c.y);lineTo(c.x,c.y+th*.34f);close()}
      drawPath(roof,Color(0xFFB86B4F));drawRect(Color(0xFF7C9AA1),Offset(c.x-tw*.17f,c.y+h*.35f),Size(tw*.07f,h*.18f));drawRect(Color(0xFF8C674B),Offset(c.x-tw*.035f,c.y+h*.48f),Size(tw*.08f,h*.28f))
     }
     BuildTool.MARKET,BuildTool.WORKSHOP,BuildTool.SCHOOL,BuildTool.CLINIC -> {
      val wall=when(b.kind){BuildTool.SCHOOL->Color(0xFFE8D6A8);BuildTool.CLINIC->Color(0xFFE7E8DE);BuildTool.WORKSHOP->Color(0xFFC9B9A2);else->Color(0xFFE6D6B9)}
      drawRect(wall,Offset(c.x-tw*.22f,c.y+h*.18f),Size(tw*.44f,h*.55f))
      drawRect(if(b.kind==BuildTool.CLINIC) Color(0xFFB75B55) else Color(0xFF9B6C4E),Offset(c.x-tw*.27f,c.y-h*.02f),Size(tw*.54f,h*.16f))
      drawRect(if(b.kind==BuildTool.SCHOOL) Color(0xFFDBA94D) else Color(0xFFE6B65D),Offset(c.x-tw*.25f,c.y-h*.12f),Size(tw*.5f,h*.17f))
     }
     BuildTool.FARM -> {drawOval(Color(0xFF6B9B52),Offset(c.x-tw*.28f,c.y),Size(tw*.58f,th*.55f));for(i in 0..3)drawLine(Color(0xFFE5C978),Offset(c.x-tw*.2f+i*tw*.1f,c.y+th*.12f),Offset(c.x-tw*.2f+i*tw*.1f,c.y+th*.43f),2.dp.toPx())}
     BuildTool.PARK -> {drawCircle(Color(0xFF477A4E),tw*.18f,Offset(c.x,c.y-th*.1f));drawCircle(Color(0xFF83B86A),tw*.14f,Offset(c.x-tw*.07f,c.y-th*.2f));drawLine(Color(0xFF795C3B),Offset(c.x,c.y-th*.1f),Offset(c.x,c.y+th*.4f),3.dp.toPx())}
     BuildTool.WELL -> {drawCircle(Color(0xFFB6A58A),tw*.16f,Offset(c.x,c.y+th*.12f));drawCircle(Color(0xFF69AFC0),tw*.1f,Offset(c.x,c.y+th*.12f))}
     BuildTool.ROAD -> {val p=Path().apply{moveTo(c.x-tw*.25f,c.y+th*.25f);lineTo(c.x,c.y+th*.5f);lineTo(c.x+tw*.25f,c.y+th*.25f);lineTo(c.x,c.y);close()};drawPath(p,Color(0xFFB8A88A))}
     else -> {}
    }
   }
   people.forEachIndexed { i,p ->
    val c=iso(p.x,p.y)
    val phase=walk+i*.85f
    val swing=sin(phase)*tw*.075f
    val stride=abs(cos(phase))*th*.035f
    val skin=listOf(Color(0xFFE9B98F),Color(0xFFD99D76),Color(0xFFF0C9A5),Color(0xFFB97855))[i%4]
    val shirt=listOf(Color(0xFF4779A8),Color(0xFFB9674F),Color(0xFF6E9561),Color(0xFF9A78A8),Color(0xFFD0A34F),Color(0xFF477F7A))[i%6]
    val hair=listOf(Color(0xFF49352B),Color(0xFF292725),Color(0xFF76503A),Color(0xFFB78A4E))[i%4]
    val unit=tw*.12f
    drawOval(Color(0x440D2418),Offset(c.x-tw*.13f,c.y+th*.19f),Size(tw*.27f,th*.14f))
    if(i==selected){drawOval(Color(0x66F3D58A),Offset(c.x-tw*.27f,c.y+th*.08f),Size(tw*.54f,th*.30f));drawCircle(Gold,tw*.29f,Offset(c.x,c.y-th*.39f),style=Stroke(2.2.dp.toPx()))}
    // Legs animate independently beneath the body
    val hip=Offset(c.x,c.y+th*.015f)
    drawLine(Color(0xFF3F4C4B),Offset(hip.x-tw*.035f,hip.y),Offset(hip.x-tw*.055f+swing,hip.y+th*.23f+stride),unit,cap=StrokeCap.Round)
    drawLine(Color(0xFF3F4C4B),Offset(hip.x+tw*.035f,hip.y),Offset(hip.x+tw*.055f-swing,hip.y+th*.23f+stride),unit,cap=StrokeCap.Round)
    // Shoes
    drawLine(Color(0xFF57483C),Offset(hip.x-tw*.055f+swing,hip.y+th*.23f+stride),Offset(hip.x-tw*.10f+swing,hip.y+th*.24f+stride),unit*.9f,cap=StrokeCap.Round)
    drawLine(Color(0xFF57483C),Offset(hip.x+tw*.055f-swing,hip.y+th*.23f+stride),Offset(hip.x+tw*.10f-swing,hip.y+th*.24f+stride),unit*.9f,cap=StrokeCap.Round)
    // Torso, shoulders and swinging arms
    drawRoundRect(shirt,Offset(c.x-tw*.105f,c.y-th*.24f),Size(tw*.21f,th*.31f),androidx.compose.ui.geometry.CornerRadius(tw*.055f))
    drawLine(skin,Offset(c.x-tw*.095f,c.y-th*.20f),Offset(c.x-tw*.16f+swing,c.y+th*.045f),unit*.72f,cap=StrokeCap.Round)
    drawLine(skin,Offset(c.x+tw*.095f,c.y-th*.20f),Offset(c.x+tw*.16f-swing,c.y+th*.045f),unit*.72f,cap=StrokeCap.Round)
    // Neck and head with hair silhouette, ears and facial features
    drawRoundRect(skin,Offset(c.x-tw*.025f,c.y-th*.31f),Size(tw*.05f,th*.10f),androidx.compose.ui.geometry.CornerRadius(unit*.3f))
    drawCircle(skin,tw*.105f,Offset(c.x,c.y-th*.405f))
    drawArc(hair,180f,180f,false,Offset(c.x-tw*.105f,c.y-th*.51f),Size(tw*.21f,th*.20f),style=Stroke(width=unit*.72f))
    drawOval(hair,Offset(c.x-tw*.102f,c.y-th*.49f),Size(tw*.20f,th*.085f))
    drawCircle(Color(0xFF3D3028),tw*.012f,Offset(c.x-tw*.035f,c.y-th*.405f))
    drawCircle(Color(0xFF3D3028),tw*.012f,Offset(c.x+tw*.035f,c.y-th*.405f))
    drawLine(Color(0xFF9A5F4A),Offset(c.x-tw*.018f,c.y-th*.365f),Offset(c.x+tw*.018f,c.y-th*.365f),1.dp.toPx(),cap=StrokeCap.Round)
   }
  }
 }
}
