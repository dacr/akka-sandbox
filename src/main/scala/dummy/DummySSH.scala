package dummy

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.util.duration._
import com.typesafe.config.ConfigFactory
import akka.actor.Props

import fr.janalyse.ssh._

object DummySSH {
	def main(args:Array[String]) {
      val system=ActorSystem("DummySSHSystem",ConfigFactory.load.getConfig("dummySSH"))
      system.actorOf(
        Props(new RobustActor(system)),
        name="RobustActor")
	}
}


class RobustActor(system:ActorSystem) extends Actor {
  
  val sh = SSH(host="localhost", username="test", password=Some("testtest")).newShell
  
  override def preStart() {
    system.scheduler.schedule(1 seconds, 5 seconds, self, "doit")
  }
  
  override def postStop() {
    sh.close()
  }
    
  def receive = {
    case "doit" => print(sh execute "date")
  }
}
