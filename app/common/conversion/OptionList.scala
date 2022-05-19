package common.conversion

object OptionList {
	def sequence[A](l: List[Option[A]]): Option[List[A]]= l match {
		case Nil => Some(Nil)
		case h :: t => h match {
			case None => None
			case Some(head) => sequence(t) match {
				case None => None
				case Some(list) => Some(head :: list)
			}
		}
	}
}
