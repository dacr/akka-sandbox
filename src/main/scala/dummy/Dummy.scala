/*
 * Copyright 2012 David Crosson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dummy

import akka.actor._
import akka.util.duration._
import akka.util.Timeout
import akka.pattern.ask
import akka.dispatch.Future

sealed trait MyMessage
case class DoItMessage(cmd:String) extends MyMessage
case class DoneMessage extends MyMessage


object Dummy {
  def main(args:Array[String]) {
    
    val howmanyjob=10*1000000
    
	import com.typesafe.config.ConfigFactory
    implicit val system=ActorSystem("DummySystem",ConfigFactory.load.getConfig("dummy"))
    
    val simu = system.actorOf(
        Props(new MySimulator(system))
        .withDispatcher("simu-dispatcher"),
        name="simulator")

    val appManager = system.actorOf(
        Props(new ApplicationManager(system, howmanyjob))
        .withDispatcher("simu-dispatcher"),
        name="application-manager")

        
    import akka.routing.RoundRobinRouter
    val processor = system.actorOf(
        Props(new MyMessageProcessor(appManager, simu))
        .withDispatcher("workers-dispatcher")
        .withRouter(RoundRobinRouter(10)),
        name="default")
        
    for(i <- 1 to howmanyjob) {
      processor ! DoItMessage("Do the job with ID#%d now".format(i))
    }
    print("All jobs sent")
  }
}

class MyMessageProcessor(appManager:ActorRef, simu:ActorRef) extends Actor {
  def receive = {
    case msg:DoItMessage =>
      implicit val timeout = Timeout(5 minutes)
      val receivedTime = System.currentTimeMillis()
      val future = simu ? msg
      future.onComplete { 
        case result:Either[Throwable, String] =>
          assert(System.currentTimeMillis()-receivedTime >= 1000)
          appManager ! DoneMessage
      }
  }
}

class MySimulator(system:ActorSystem) extends Actor {
  def receive = {
    case _:DoItMessage =>
      // Fake processing, somewhere, the job is executed and we get 
      // the results 1s later asynchronously
      system.scheduler.scheduleOnce(1000 milliseconds, sender, "Done") 
  }
}

class ApplicationManager(system:ActorSystem, howmanyjob:Int) extends Actor {
  val startedTime = System.currentTimeMillis()
  var count=0
  def receive = {
    case DoneMessage => 
      count+=1
      if (count%(howmanyjob/20)==0) println("%d/%d processed".format(count, howmanyjob))
      if (count == howmanyjob) {
        val now=System.currentTimeMillis()
        println("Everything processed in %d seconds".format((now-startedTime)/1000))
        system.shutdown() 
      }
  }
}
