package testcase.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import testcase.logic.SearchThread;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Controller {

    private Thread logSearchThread;
    private Thread showFoundFileThread;

    @FXML
    private TextField input_dir;

    @FXML
    private TextField input_text;

    @FXML
    private TextField input_ext;

    @FXML
    private Button button_search;

    @FXML
    private Button button_stop;

    @FXML
    private SplitPane split_pane;

    //    Area on the left half of app-window with tabs of pen files
    @FXML
    private TabPane tabs_files;

    @FXML
    private void clickSearch() {
        if (input_ext.getText().equals("")) {
            input_ext.setText("log");
        }

        if (input_dir.getLength() != 0 || input_text.getLength() != 0) {
            List<File> filesQueue = new ArrayList<>();

            SearchThread searchThread = new SearchThread(
                    new File(input_dir.getText()),
                    input_ext.getText(),
                    input_text.getText(),
                    filesQueue);

            FoundFileThread foundFileThread = new FoundFileThread(filesQueue);

            foundFileThread.updateFileTreeView();

            logSearchThread = new Thread(searchThread, "LogSearchThread");
            showFoundFileThread = new Thread(foundFileThread, "ShowFoundFileThread");

            logSearchThread.start();
            showFoundFileThread.start();

            button_stop.setDisable(false);
            button_search.setDisable(true);
        }
    }

    @FXML
    private void clickStop() {
        logSearchThread.interrupt();
        showFoundFileThread.interrupt();
        System.out.println("SearchThread and showFoundFileThread was interrupted by stopButton");
        button_search.setDisable(false);
    }

    public class FileTreeCell extends TextFieldTreeCell<File> {
        FileTreeCell() {
            super();
            this.setOnMouseClicked(event -> {
                TreeItem<File> treeItem = getTreeItem();
                if (treeItem.isLeaf() && event.getClickCount() == 2) {
                    new Thread(() -> openFileInNewTab(treeItem.getValue())).start();
                }
            });
        }
    }

    private void openFileInNewTab(File file) {
        ObservableList<String> lines = FXCollections.observableArrayList();
        ListView<String> listViewFile = new ListView<>();
        listViewFile.setOrientation(Orientation.VERTICAL);
        Tab fileTab = new Tab(file.toString());
        Platform.runLater(() -> tabs_files.getTabs().add(fileTab));
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                lines.add(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        listViewFile.setItems(lines);
        Platform.runLater(() -> fileTab.setContent(listViewFile));
    }

    public class FoundFileThread implements Runnable {
        private final List<File> filesQueue;
        TreeItem<File> root = new TreeItem<>(new File(input_dir.getText().split("[/\\\\]")[0]));
        TreeView<File> customTreeView = new TreeView<>();

        FoundFileThread(List<File> filesQueue) {
            this.filesQueue = filesQueue;
        }

        @Override
        public void run() {
            createFileTreeView();
//            it's not good way
            while (true) {
                try {
                    showFoundFile();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        void showFoundFile() throws InterruptedException {
            synchronized (filesQueue) {
                while (filesQueue.isEmpty()) {
                    System.out.println("List of FoundFiles is empty...");
                    filesQueue.wait();
                }
                File file = filesQueue.get(filesQueue.size() - 1);
                updateFileTreeView(file);
                System.out.println("File found: " + file);
                filesQueue.remove(file);
            }
        }

        void createFileTreeView() {
            Platform.runLater(() -> {
                customTreeView.setRoot(root);
                customTreeView.setMinSize(300, 540);
                split_pane.getItems().add(customTreeView);
            });
        }

        void updateFileTreeView(File file) {
            String[] filePath = file.toString().split("[/\\\\]");
//            TODO: MAKE IT GREAT AGAIN
            TreeItem<File> rootPath = root;
            int i = 0;
            for (String dir : filePath) {
                TreeItem<File> itemFile = new TreeItem<>(new File(dir));
                rootPath = itemFile;
                if (!rootPath.getChildren().contains(itemFile)) {
                    if (i == 0){
                        rootPath = itemFile;
                        root.getChildren().add(itemFile);
                        i++;
                    } else {
                        rootPath.getChildren().add(itemFile);
                    }
                }
            }
            updateFileTreeView();
            TreeItem<File> finalRootPath = rootPath;
            Platform.runLater(() -> {
//                TreeItem<File> itemFile = new TreeItem<>(file);
                root.setExpanded(true);
//                root.getChildren().add(itemFile);
                customTreeView.setCellFactory(treeView -> new FileTreeCell());
                customTreeView.setRoot(finalRootPath);
                customTreeView.setMinSize(300, 540);
                split_pane.getItems().add(customTreeView);
            });
        }

        void updateFileTreeView() {
            Platform.runLater(() -> {
                if (split_pane.getItems().size() > 1) {
                    split_pane.getItems().remove(1);
                }
            });
        }
    }
}
