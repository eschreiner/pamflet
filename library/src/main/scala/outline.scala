package pamflet
import com.tristanhunt.knockoff._
import PamfletDiscounter.headerText

object Outline {
  private case class Return(nodes: xml.NodeSeq, rest: Seq[Header])
  def apply(page: AuthoredPage) = {
    def anchor(name: String) =
      <a href={Printer.webify(page.name) +
               BlockNames.fragment(name)}>{ name }</a>

    def build(blocks: Seq[Header], cur: Int): Return =
      blocks match {
        case Seq(a, b, tail @_*) if a.level == cur && b.level > cur =>
          val nested = build(b +: tail, b.level)
          val after = build(nested.rest, cur)
          val name = headerText(a.spans)
          Return((
            <li> { anchor(name) }
              <ul class="outline"> { nested.nodes } </ul>
            </li>
          ) ++ after.nodes, after.rest)
        case Seq(a, tail @ _*) if a.level > cur =>
          val Return(nodes, rest) = build(blocks, a.level)
          Return(nodes, rest)
        case Seq(a, tail @ _*) if a.level == cur =>
          val Return(nodes, rest) = build(tail, cur)
          val name = headerText(a.spans)
          Return(( <li> { anchor(name) } </li> ) ++ nodes, rest)
        case _ =>
          Return(Seq.empty, blocks)
      }

    <ul class="outline"> {
      build(page.blocks.collect {
        case h: Header => h
      }.tail, 0).nodes
    } </ul>
  }
}
