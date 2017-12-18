package by.andrej.cd.app

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.{BorderPane, GridPane, Priority}
import scalafx.stage.FileChooser

object Application extends JFXApp {
  //set up files list
  val fileListView = new ListView[String]
  val addFileButton = new Button {
    text = "Add file"
    prefWidth = 120
    prefHeight = 35
    onMouseClicked = _ => {
      val files = (new FileChooser).showOpenMultipleDialog(stage)
      if (files != null) {
        val pathes = files.foldLeft(List[String]())((r, f) => f.getPath :: r)
        fileListView.items.getValue.addAll(ObservableBuffer(pathes))
      }
    }
  }
  val delFileButton = new Button {
    text = "Delete file"
    prefWidth = 120
    prefHeight = 35
    onMouseClicked = _ => {
      val sel = fileListView.selectionModel.value.getSelectedIndices
      val items = fileListView.items.getValue
      items.removeIf(f => sel.contains(items.indexOf(f)))
    }
  }
  val leftPane = new GridPane {
    add(fileListView, 0, 0, 2, 1)
    add(addFileButton, 0, 1)
    add(delFileButton, 1, 1)
    hgap = 10
  }
  //set up text area
  val logTextArea = new TextArea
  //set up bottom pane
  val burnButton = new Button("Burn")
  val progressBar = new ProgressBar {
    prefWidth = Int.MaxValue
  }
  val bottomPane = new GridPane {
    add(progressBar, 0, 0, 3, 1)
    add(burnButton, 3, 0)
    margin = Insets(20)
    hgap = 20
    hgrow = Priority.Always
  }

  stage = new PrimaryStage {
    title ="CD Burner"
    resizable = false
    scene = new Scene(700, 500) {
      //set up root pane
      val rootPane = new BorderPane
      rootPane.bottom = bottomPane
      rootPane.left = leftPane
      rootPane.center = logTextArea
      root = rootPane
    }
  }
}
