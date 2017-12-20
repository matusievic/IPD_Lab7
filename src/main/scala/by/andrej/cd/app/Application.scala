package by.andrej.cd.app

import java.awt
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{PopupMenu, SystemTray, TrayIcon}
import java.io.File
import javax.imageio.ImageIO
import javax.swing.event.{ChangeEvent, ChangeListener}

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
  //set up tray
  val tray = new TrayIcon(ImageIO.read(new File("src/main/resources/icon.png")), "") {
    if (SystemTray.isSupported) SystemTray.getSystemTray.add(this)
    addActionListener(_ => Platform.runLater(stage.setIconified(false)))
  }
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
      val files = fileListView.items.get.toString;
      if (burner.getState != Thread.State.NEW) burner = new Burner(begin, update, end)
      burner.files = files.substring(1, files.length - 1).split(", ").toList
      burner.eject = ejectCheckBox.selected.value
      burner.start()
    }
  }
  val progressBar = new ProgressBar {
    prefWidth = Int.MaxValue
    progress = 0
  }
  val ejectCheckBox = new CheckBox("Eject after burning")
  val bottomPane = new GridPane {
    add(ejectCheckBox, 0, 0)
    add(progressBar, 1, 0, 3, 1)
    add(burnButton, 4, 0)
    margin = Insets(20)
    hgap = 20
    hgrow = Priority.Always
  }
  //set up burner thread
  var burner: Burner = new Burner(begin, update, end)
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
    onCloseRequest.setValue(_ => System.exit(0))
  }

  def begin(): Unit = Platform.runLater {
    logTextArea.text.value = ""
    addFileButton.disable = true
    delFileButton.disable = true
    burnButton.disable = true
  }

  def update(s: String): Unit = Platform.runLater {
    if (s.matches("Track \\d+:.+of.+")) {
      val curBegin = s.indexOf(':') + 1
      val curEnd = s.indexOf("of") - 1
      val totalBegin = curEnd + 3
      val totalEnd = s.indexOf("MB") - 1
      val cur = s.substring(curBegin, curEnd).replaceAll("\\s", "").toDouble
      val total = s.substring(totalBegin, totalEnd).replaceAll("\\s", "").toDouble
      progressBar.progress = cur / total
    }
    logTextArea.appendText(s + '\n')
    tray.setToolTip(s)
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
