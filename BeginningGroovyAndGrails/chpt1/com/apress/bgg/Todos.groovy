/*
 * Groovy example showing how much less code is required for 
 * the equivilant Todo.java class.
 */
package com.apress.bgg.groovy;

class Todo {

  String name
  String note

}

def todos = [
  new Todo(name:"1", note:"one"),
  new Todo(name:"2", note:"two"),
  new Todo(name:"3", note:"three")
]

todos.each {
  println "${it.name} ${it.note}"
}
