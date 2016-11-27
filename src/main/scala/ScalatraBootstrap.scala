import javax.servlet.ServletContext

import com.innopolis.ir.web.Servlet
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new Servlet, "/*")
  }
}
