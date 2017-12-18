package by.andrej.cd.app

import by.andrej.cd.service.Burner

import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
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
  val burnButton = new Button {
    text = "Burn"
    onMouseClicked = _ => {
      burner.files = fileListView.items.get.toString.split('\n').toList.map(f => f.substring(1, f.length - 1))
      burner.eject = false
      burner.start()
    }
  }
  val progressBar = new ProgressBar {
    prefWidth = Int.MaxValue
    progress = 0
  }
  val bottomPane = new GridPane {
    add(progressBar, 0, 0, 3, 1)
    add(burnButton, 3, 0)
    margin = Insets(20)
    hgap = 20
    hgrow = Priority.Always
  }
  //set up burner thread
  val burner = new Burner(begin, update, end)
  // set up stage
  stage = new PrimaryStage {
    title = "CD Burner"
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

  def begin(): Unit = Platform.runLater {
    addFileButton.disable = true
    delFileButton.disable = true
    burnButton.disable = true
  }

  def update(s: String): Unit = Platform.runLater {
    logTextArea.appendText(s + '\n')
  }

  def end(res: Int): Unit = Platform.runLater {
    if (res == 0) new Alert(AlertType.Information, "Burning has successfully finished").showAndWait()
    else new Alert(AlertType.Error, "An error has occurred").showAndWait()
    addFileButton.disable = false
    delFileButton.disable = false
    burnButton.disable = false
    progressBar.progress = 0
  }
}
