package edt.ca.utoronto.individual;

import com.github.davidbolet.jpascalcoin.api.client.PascalCoinClient;
import com.github.davidbolet.jpascalcoin.api.client.PascalCoinClientImpl;
import com.github.davidbolet.jpascalcoin.api.model.Account;
import com.github.davidbolet.jpascalcoin.api.model.DecryptedPayload;
import com.github.davidbolet.jpascalcoin.api.model.Operation;
import com.github.davidbolet.jpascalcoin.api.model.PublicKey;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class MainController {

    PascalCoinClient client;

    @FXML
    JFXComboBox<Account> acc_drop;

    @FXML
    JFXTabPane tabs;

    @FXML
    StackPane stackPane;

    @FXML
    Tab acct;

    @FXML
    Tab blkt;

    @FXML
    Label name;

    @FXML
    Label balance;

    @FXML
    JFXButton refresh;

    @FXML
    ImageView qr;

    @FXML
    JFXTextField amountt;

    @FXML
    JFXTextField payloadt;

    @FXML
    JFXListView<BorderPane> translist;

    private static final String PASCALIO = "pascalio";
    private static final String BLANK = "voidoper";

    private Account cur;
    private ArrayList<Account> accounts;
    private ArrayList<PublicKey> keys;
    private String[] keysArray;
    private DateFormat format;

    public void initialize() {
        client = new PascalCoinClientImpl("localhost", (short) 4003);
        format = new SimpleDateFormat("MMM dd yyyy hh:mm zzz");
        translist.getStyleClass().add("mylistview");
        accounts = new ArrayList<>();
        keys = new ArrayList<>();
        acc_drop.getSelectionModel().selectedItemProperty().addListener((a1, a2, a3) -> {
            cur = a3;
            getAndSetAccount(a3);
        });

        translist.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends BorderPane> a1, BorderPane a2, BorderPane a3) -> {
            if (a3 != null) {
                Operation op = (Operation) a3.getUserData();
                Account acc = getAccount(op.getAccount());

                ScrollPane scroll = new ScrollPane();
                scroll.setPrefWidth(400);
                scroll.setMaxHeight(500);
                scroll.setMaxWidth(400);
                scroll.setFitToWidth(true);
                VBox container = new VBox();
                scroll.setContent(container);
                scroll.getStyleClass().add("mylistview");
                scroll.getStyleClass().add("dialog-pane");

                container.setPadding(new Insets(25, 25, 25, 25));
                container.getStyleClass().add("dialog-pane");
                container.setSpacing(10);

                Label time1 = new Label("Time:");
                time1.getStyleClass().add("subtitle-label");
                time1.setWrapText(true);

                Label time2 = new Label(format.format(new Date(op.getTime() * 1000)));
                time2.getStyleClass().add("normal-label-white");
                time2.setWrapText(true);

                Separator sep1 = new Separator(Orientation.HORIZONTAL);

                Label op1 = new Label("Operation:");
                op1.getStyleClass().add("subtitle-label");
                op1.setWrapText(true);

                Label op2 = new Label(op.getTypeDescriptor());
                op2.getStyleClass().add("normal-label-white");
                op2.setWrapText(true);

                Label op3 = new Label(String.format("in block %d/%d", op.getBlock(), op.getOperationBlock()));
                op3.getStyleClass().add("normal-label-white");
                op3.setWrapText(true);
                Separator sep2 = new Separator(Orientation.HORIZONTAL);

                Label pay1 = new Label("Hex Payload:");
                pay1.getStyleClass().add("subtitle-label");
                pay1.setWrapText(true);


                /**
                 * payload decrypt
                 */
                byte[] b = op.getPayLoad();
                String hexpay = Base64.getEncoder().withoutPadding().encodeToString(b);
                System.out.println(hexpay);

                byte[] fixed = new byte[b.length];
                for (int i = 0; i < b.length / 4f; i++) {
                    for (int j = 0; j < 4; j++) {
                        fixed[i * 4 + j] = b[i * 4 + 3 - j];
                    }
                }

                StringBuilder sb = new StringBuilder();
                for(byte bb: fixed){
                    sb.append(String.format("%02X", bb));
                }

                System.out.println(sb.toString());
                DecryptedPayload p = client.payloadDecrypt(hexpay, null);
                boolean found = p != null;

                Label pay2 = new Label(hexpay.isEmpty() ? "<No Payload>" : hexpay);

                pay2.getStyleClass().add("normal-label-white");
                pay2.setWrapText(true);

                Separator sep3 = new Separator(Orientation.HORIZONTAL);

                Label pay3 = new Label("Payload:");
                pay3.getStyleClass().add("subtitle-label");
                pay3.setWrapText(true);

                Label pay4 = new Label(hexpay.isEmpty() ? "<No Payload>" : (found ? p.getUnencryptedPayload() : "Could not decrypt"));
                pay4.getStyleClass().add(found ? "normal-label-white" : "normal-label-red");
                pay4.setWrapText(true);

                Separator sep4 = new Separator(Orientation.HORIZONTAL);

                Label ophash1 = new Label("Operation Hash:");
                ophash1.getStyleClass().add("subtitle-label");
                ophash1.setWrapText(true);

                Label ophash2 = new Label(op.getOpHash());
                ophash2.getStyleClass().add("normal-label-white");
                ophash2.setWrapText(true);

                ImageView ophash3 = new ImageView(getQR(op.getOpHash()));


                container.getChildren().addAll(time1, time2, sep1, op1, op2, op3, sep2, pay1, pay2, sep3, pay3, pay4, sep4, ophash1, ophash2, ophash3);

                JFXDialog dia = new JFXDialog(stackPane, scroll, JFXDialog.DialogTransition.BOTTOM);

                dia.setOnDialogClosed(a -> translist.getSelectionModel().clearSelection());
                dia.show();
            }
        });

        amountt.textProperty().addListener((a1, a2, a3) -> {
            if (!a3.isEmpty())
                try {
                    Double.valueOf(a3);
                } catch (NumberFormatException ignored) {
                    amountt.setText(a2);
                }
        });
        payloadt.textProperty().addListener((a1, a2, a3) -> {
            if (a3.length() > 100)
                payloadt.setText(a2);
        });

        acc_drop.setConverter(new StringConverter<Account>() {
            @Override
            public String toString(Account account) {
                int num = account.getAccount();
                int check = (num * 101) % 89 + 10;
                String account_key = account.getName().equals("") ? account.getAccount().toString() : account.getName();
                return String.format("%d-%d (%.4f)", num, check, account.getBalance());
//                return account.getAccount().toString() + ": " + String.format("%.4f", account.getBalance());
            }

            @Override
            public Account fromString(String string) {
                return null;
            }
        });

        refresh.setOnAction(a -> {
            qr.setImage(getQR(PASCALIO, cur.getAccount(), Double.valueOf(amountt.getText().isEmpty() ? "0" : amountt.getText()), payloadt.getText()));
        });
        setAccounts();

    }

    private Account getAccount(int num) {
        for (Account a : accounts) {
            if (a.getAccount() == num)
                return a;
        }
        return null;
    }

    private void getAndSetAccount(Account a3) {

        new Thread(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ArrayList<BorderPane> nodes = new ArrayList<>();

                List<Operation> ops = client.getAccountOperations(a3.getAccount(), null, null, null);
//                ops.addAll(client.getPendings());
                ops.forEach(a -> {

                    BorderPane sides = new BorderPane();

                    sides.setUserData(a);

                    VBox left = new VBox();
                    Label y = new Label(format.format(new Date(a.getTime() * 1000)));
                    Label z = new Label(a.getTypeDescriptor());
                    Double amount = a.getAmount();
                    String amountString = amount == 0d ? "" : String.format("%s%.4f PASC", amount >= 0 ? "+" : "", amount);

                    Label x = new Label(amountString);

                    x.getStyleClass().add("subtitle-label");
                    y.getStyleClass().add("normal-label");
                    z.getStyleClass().add("normal-label");
                    z.setStyle("-fx-text-fill:white");

                    left.getChildren().add(y);
                    left.getChildren().add(z);

                    sides.setLeft(left);
                    sides.setRight(x);

                    nodes.add(sides);
                });

                Platform.runLater(() -> {
                    translist.getItems().clear();
                    name.setText("Account name: " + (a3.getName().isEmpty() ? "<unnamed>" : a3.getName()));
                    balance.setText(String.format("%.4f", a3.getBalance()));
                    qr.setImage(getQR(BLANK, a3.getAccount(), 0.0d, ""));
                    translist.getItems().addAll(nodes);
                });
                return null;
            }
        }).start();
    }

    private Image getQR(String op, int num, double amount, String payload) {
        return getQR(String.format("op[\"%s\"]::to[\"%d-%d\"]::with[%.4f]::as[\"%s\"]", op, num, (num * 101) % 89 + 10, amount, payload));
    }

    private Image getQR(String str) {
        File io = QRCode.from(str).to(ImageType.JPG).withSize(250, 250).file();
        Image i = null;
        try {
            i = SwingFXUtils.toFXImage(ImageIO.read(io), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return i;
    }

    public void setAccounts() {
        new Thread(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                accounts.clear();
                keys.clear();
                keys.addAll(client.getWalletPubKeys(null, null));
                keysArray = new String[keys.size() * 2];

                for (int i = 0; i < keys.size(); i++) {
                    keysArray[2 * i] = keys.get(i).getEncPubKey();
                    keysArray[2 * i + 1] = keys.get(i).getBase58PubKey();
                }

                List<Account> accountslist = client.getWalletAccounts(null, null, null, null);
                accounts.addAll(accountslist);
                Platform.runLater(() ->
                        acc_drop.getItems().addAll(accountslist)
                );
                return null;
            }
        }).start();

    }


}