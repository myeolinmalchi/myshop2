package models

object CodeGen extends App {
	slick.codegen.SourceCodeGenerator.run(
		"slick.jdbc.MySQLProfile",
		"com.mysql.cj.jdbc.Driver",
		"jdbc:mysql://localhost:3306/myshop2",
		"C:/Users/minsu/Desktop/workspace/myshop/app/",
		"models", Option("root"), Option("382274"), true, false
	)
}
