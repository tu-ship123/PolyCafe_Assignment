/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package poly.cafe.ui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import poly.cafe.dao.BillDAO;
import poly.cafe.dao.BillDetailDAO;
import poly.cafe.entity.Bill;
import poly.cafe.entity.BillDetail;
import poly.cafe.util.MsgBox;
import poly.cafe.util.XAuth;

/**
 *
 * @author acer
 */
public class BillJDialog extends javax.swing.JDialog {
    
    private int cardId;
    BillDAO dao = new BillDAO();
    BillDetailDAO daodt = new BillDetailDAO();
    private long billId = 0;

    // Constructor chạy: Dùng để truyền cardId
    public BillJDialog(Dialog owner, boolean modal, int cardId) {
        super(owner, modal);
        initComponents();
        this.cardId = cardId;
        setTitle("HÓA ĐƠN - Thẻ Khách Hàng #" + cardId);
        setLocationRelativeTo(owner);  // căn giữa so với cửa sổ cha
        edit(); // show Bill và BillDetail
        
        tblBillDetails.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblBillDetails.getSelectedRow();
                    if (row >= 0) {
                        Long billId = Long.valueOf(tblBillDetails.getValueAt(row, 0).toString());
                        String drinkId = tblBillDetails.getValueAt(row, 1).toString();

                        BillDetail bd = daodt.selectByBillDrinkId(billId, drinkId);
                        if (bd != null) {
                            String input = JOptionPane.showInputDialog(null, "Nhập số lượng mới:", bd.getQuantity());
                            try {
                                int newQty = Integer.parseInt(input);
                                if (newQty > 0) {
                                    bd.setQuantity(newQty);
                                    daodt.update(bd);
                                    fillTableBillDetails();
                                } else {
                                    MsgBox.alert(null, "Số lượng phải > 0");
                                }
                            } catch (NumberFormatException ex) {
                                MsgBox.alert(null, "Số không hợp lệ");
                            }
                        }
                    }
                }
            }
        });
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                long id = Long.parseLong(txtId.getText());
                List<BillDetail> details = daodt.selectBillId(id);
                if (details.isEmpty()) {
                    dao.delete(id);
                }
            }
        });
    }
    
    void fillTableBillDetails() {
        DefaultTableModel model = (DefaultTableModel) tblBillDetails.getModel();
        model.setRowCount(0);
        try {
            billId = Long.parseLong(txtId.getText());
            List<BillDetail> list = daodt.selectBillId(billId); // gọi đúng tên hàm DAO
            for (BillDetail cd : list) {
                Object[] row = {
                    cd.getBillId(), cd.getDrinkId(), cd.getUnitPrice(), cd.getDiscount(), cd.getQuantity(),
                    cd.getUnitPrice() * (1 - cd.getDiscount()) * cd.getQuantity()
                };
                model.addRow(row);
            }
        } catch (Exception e) {
            MsgBox.alert(this, "Lỗi truy vấn dữ liệu!");
        }
    }

    void setFormBill(Bill bill) {
        txtId.setText(bill.getId().toString());
        txtCardId.setText(bill.getCardId().toString());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        txtCheckin.setText(sdf.format(bill.getCheckin()));

        txtUsername.setText(bill.getUsername());
        txtStatus.setText("Chưa thanh toán");
        txtCheckout.setText("");
    }

    public Bill getFormBill() {
        try {
            Bill bill = new Bill();
            bill.setId(Long.valueOf(txtId.getText()));
            bill.setCardId(Integer.valueOf(txtCardId.getText()));
            bill.setUsername(XAuth.user.getUsername());

            String input = txtCheckin.getText();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date checkinDate = sdf.parse(input); // có thể ném ParseException
            bill.setCheckin(checkinDate); // hoặc new java.sql.Date(checkinDate.getTime()) nếu cần

            //SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String formattedNow = sdf.format(new Date());
            txtCheckout.setText(formattedNow);

            bill.setStatus(0);
            return bill;
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "Ngày check-in không đúng định dạng! Định dạng đúng là: dd/MM/yyyy HH:mm:ss");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID hoặc Card ID không hợp lệ! Phải là số.");
        }
        return null;
    }

    void edit() {
        Bill bill = dao.selectByIDStatus0(cardId);
        if (bill == null) {
            //billId = dao.selectMaxBillId() + 1; // tạo billid mới +1
            txtId.setText(String.valueOf(billId));
            txtCardId.setText(String.valueOf(cardId));

            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            txtCheckin.setText(sdf.format(now));

            txtUsername.setText(XAuth.user.getUsername());
            txtStatus.setText("Chưa thanh toán");
            txtCheckout.setText("");

            // Sau khi insert xong, load lại form
            insert();
            Bill insertedBill = dao.selectByIDStatus0(cardId);
            if (insertedBill != null) {
                setFormBill(insertedBill);
                //txtId.setText(String.valueOf(insertedBill.getId()));
            }
        } else {
            setFormBill(bill);
            fillTableBillDetails();
        }
    }

    // Constructor ko chạy: Để đảm bảo tương thích với lệnh gọi cũ
    public BillJDialog(Frame owner, boolean modal) {
        super(owner, modal);
        initComponents();
    }

    private void showDRINKDIALOG(long billId) {
        // Mở BillManagerJDialog và truyền int billId
        DRINKJDIALOG drink = new DRINKJDIALOG(this, true, (int) billId);
        drink.setVisible(true);
        fillTableBillDetails();
    }

    void insert() {
        Bill cd = getFormBill();
        try {
            dao.insert(cd);
            MsgBox.alert(this, "Thêm dữ liệu thành công");
        } catch (Exception e) {
            MsgBox.alert(this, "Lỗi: " + e.getMessage());
        }
    }

    private void removeDrink() {
    boolean deleted = false;

    for (int i = tblBillDetails.getRowCount() - 1; i >= 0; i--) {
        Object check = tblBillDetails.getValueAt(i, tblBillDetails.getColumnCount() - 1); // Cột cuối là checkbox
        if (check != null && (Boolean) check) {
            Long billId = Long.parseLong(tblBillDetails.getValueAt(i, 0).toString());
            String drinkId = tblBillDetails.getValueAt(i, 1).toString();

            BillDetail bd = daodt.selectByBillDrinkId(billId, drinkId);
            if (bd != null) {
                try {
                    daodt.delete(bd.getId());
                    deleted = true;
                } catch (Exception e) {
                    MsgBox.alert(this, "Lỗi khi xoá đồ uống: " + e.getMessage());
                }
            }
        }
    }

    if (deleted) {
        fillTableBillDetails();
        MsgBox.alert(this, "Đã xoá các đồ uống đã chọn khỏi chi tiết hoá đơn.");
    } else {
        MsgBox.alert(this, "Vui lòng chọn ít nhất một đồ uống để xoá.");
    }
}
    
    private void checkout() {
    Bill bill = getFormBill();
    if (bill != null) {
        bill.setStatus(1); // Trạng thái đã thanh toán
        bill.setCheckout(new Date());
        dao.update(bill);
        txtStatus.setText("Đã thanh toán");
        txtCheckout.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(bill.getCheckout()));
        MsgBox.alert(this, "Thanh toán thành công!");
    }
}
    private void cancelBill() {
    List<BillDetail> details = daodt.selectBillId(Long.parseLong(txtId.getText()));
    if (details.isEmpty()) {
        dao.delete(Long.parseLong(txtId.getText()));
        MsgBox.alert(this, "Đã xóa hóa đơn vì không có đồ uống.");
        this.dispose();
    } else {
        Bill bill = getFormBill();
        if (bill != null) {
            bill.setStatus(-1); // Trạng thái bị hủy
            dao.update(bill);
            txtStatus.setText("Đã hủy");
            MsgBox.alert(this, "Đã hủy hóa đơn.");
        }
    }
}
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtCardId = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtCheckin = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtStatus = new javax.swing.JTextField();
        txtCheckout = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblBillDetails = new javax.swing.JTable();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnCheckout = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Mã phiếu");

        jLabel2.setText("Thẻ số");

        jLabel3.setText("Thời điểm đặt hàng");

        jLabel4.setText("Nhân viên");

        jLabel5.setText("Trạng thái");

        jLabel6.setText("Thời điểm thanh toán");

        tblBillDetails.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Mã phiếu", "Đồ uống", "Đơn giá", "Giảm giá", "Số lượng", "Thành tiền", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblBillDetails.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblBillDetailsMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblBillDetails);

        btnAdd.setText("Thêm đồ uống");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnRemove.setText("Xóa đồ uống");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        btnCheckout.setText("Thanh toán");
        btnCheckout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckoutActionPerformed(evt);
            }
        });

        btnCancel.setText("Hủy tiền");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCardId, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCheckin)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtCheckout, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 6, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(116, 116, 116)
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCheckout)
                        .addGap(18, 18, 18)
                        .addComponent(btnCancel)
                        .addGap(18, 18, 18))))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(16, 16, 16)
                    .addComponent(btnRemove)
                    .addContainerGap(587, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCardId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCheckin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCheckout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnCheckout)
                    .addComponent(btnCancel))
                .addContainerGap(7, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(271, Short.MAX_VALUE)
                    .addComponent(btnRemove)
                    .addContainerGap()))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        BillJDialog.this.showDRINKDIALOG(billId);
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        // TODO add your handling code here:
        removeDrink();
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnCheckoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckoutActionPerformed
        // TODO add your handling code here:
        checkout();
    }//GEN-LAST:event_btnCheckoutActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        // TODO add your handling code here:
        cancelBill();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void tblBillDetailsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBillDetailsMouseClicked
        // TODO add your handling code here:        
    }//GEN-LAST:event_tblBillDetailsMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(BillJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BillJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BillJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BillJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                BillJDialog dialog = new BillJDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCheckout;
    private javax.swing.JButton btnRemove;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tblBillDetails;
    private javax.swing.JTextField txtCardId;
    private javax.swing.JTextField txtCheckin;
    private javax.swing.JTextField txtCheckout;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtStatus;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
