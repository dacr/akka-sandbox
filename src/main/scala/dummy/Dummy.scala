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
import akka.dispatch.Await
import akka.pattern.ask
import akka.pattern.gracefulStop
import akka.dispatch.Future
import java.io.Closeable

sealed trait MyMessage
case class DoItMessage(cmd:String) extends MyMessage
case class EndMessage extends MyMessage


object Dummy {
  def main(args:Array[String]) {
	import com.typesafe.config.ConfigFactory
    implicit val system=ActorSystem("DummySystem",ConfigFactory.load.getConfig("dummy"))
    
    val simu = system.actorOf(
        Props(new MySimulator(system))
        .withDispatcher("simu-dispatcher"),
        name="simulator")
    
    import akka.routing.RoundRobinRouter
    val processor = system.actorOf(
        Props(new MyMessageProcessor(simu))
        .withDispatcher("workers-dispatcher")
        .withRouter(RoundRobinRouter(10)),
        name="default")

    // Strange... looks like this is never called although gracefulStop on simu and processor...
    system.registerOnTermination( { println("all actors terminated"); system.shutdown } )
        
    for(i <- 1 to 100) {
      processor ! DoItMessage("Do the job with ID#%d now".format(i))
    }
    //print("All jobs sent")
	try {	  
	  
	  val stoppingProcessor: Future[Boolean] = gracefulStop(processor, 10 seconds)
	  Await.result(stoppingProcessor, 11 seconds)

	  //system.scheduler match { case x:Closeable => x.close() case _ => }
	  
	  val stoppingSimulator: Future[Boolean] = gracefulStop(simu, 10 seconds)
	  Await.result(stoppingSimulator, 11 seconds)	  
	  
	  //system.shutdown()
	  println("Finished")
	} catch {
	  case e: ActorTimeoutException => println("the actor wasn't stopped within 10 second")
	  case e: Exception => //e.printStackTrace()
	}
  }
}


class MyMessageProcessor(simu:ActorRef) extends Actor {
  def receive = {
    case msg:DoItMessage =>
      implicit val timeout = Timeout(10 seconds)
      val future = simu ? msg
      future.onSuccess { 
        case msg:String => print("O")
      }
      print("o")
  }
}

class MySimulator(system:ActorSystem) extends Actor {
  def receive = {
    case _:DoItMessage => system.scheduler.scheduleOnce(1000 milliseconds, sender, "Done") // Fake processing
  }
}
