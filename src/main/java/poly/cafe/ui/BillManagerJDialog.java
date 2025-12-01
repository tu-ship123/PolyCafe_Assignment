/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package poly.cafe.ui;

import java.awt.Component;
import java.awt.Frame;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import poly.cafe.dao.BillDAO;
import poly.cafe.dao.BillDetailDAO;
import poly.cafe.entity.Bill;
import poly.cafe.entity.BillDetail;
import poly.cafe.util.MsgBox;
import poly.cafe.util.TimeRange;
import poly.cafe.util.XDate;
import poly.cafe.util.XImage;

/**
 *
 * @author khánh
 */
public class BillManagerJDialog extends javax.swing.JDialog {
    
    BillDAO daoBill = new BillDAO();
    BillDetailDAO daoDetail = new BillDetailDAO();
    
    int row = -1; // Hàng được chọn trên bảng

    // Constructor phụ: Để đảm bảo tương thích với lệnh gọi cũ
    public BillManagerJDialog(Frame owner, boolean modal) {
        super(owner, modal);
        initComponents();
        init();
    }

    void init() {
        setIconImage(XImage.getAppIcon());
        setLocationRelativeTo(null);
        setTitle("QUẢN LÝ PHIẾU BÁN HÀNG");
        cboTimeRanges.addActionListener(e -> selectTimeRange());
        clearForm();
    }
    
    private boolean validateDate() {
    String beginText = jTextField1.getText().trim();
    String endText = txtEnd.getText().trim();

    if (beginText.isEmpty() || endText.isEmpty()) {
        MsgBox.alert(this, "Vui lòng nhập đầy đủ cả hai ngày.");
        return false;
    }

    Date begin, end;
    try {
        begin = XDate.parse(beginText, "dd/MM/yyyy");
        end = XDate.parse(endText, "dd/MM/yyyy");
    } catch (Exception e) {
        MsgBox.alert(this, "Ngày nhập không đúng định dạng (dd/MM/yyyy).");
        return false;
    }

    if (begin.after(end)) {
        MsgBox.alert(this, "'Từ ngày' phải trước hoặc bằng 'Đến ngày'.");
        return false;
    }

    return true;
}
    
    void fillTableBill() {
    DefaultTableModel model = (DefaultTableModel) tblBill.getModel();
    model.setRowCount(0);

    // Khai báo ngày
    Date begin = XDate.parse(jTextField1.getText(), "dd/MM/yyyy");
    Date end = XDate.parse(txtEnd.getText(), "dd/MM/yyyy");

    // Cập nhật end đến 23:59:59
    Calendar cal = Calendar.getInstance();
    cal.setTime(end);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 999);
    end = cal.getTime();

    try {
        List<Bill> list = daoBill.findByTimeRange(begin, end);
        for (Bill cd : list) {
            // Chuyển trạng thái từ số sang chữ
            String statusText;
            switch (cd.getStatus()) {
                case -1:
                    statusText = "Lỗi";
                    break;
                case 0:
                    statusText = "Đang phục vụ";
                    break;
                case 1:
                    statusText = "Hoàn tất";
                    break;
                default:
                    statusText = "Không xác định";
                    break;
            }

            Object[] row = {
                cd.getId(),
                cd.getCardId(),
                cd.getCheckin(),
                cd.getCheckout(),
                statusText,  // trạng thái hiển thị chữ
                cd.getUsername()
            };
            model.addRow(row);
        }
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi truy vấn dữ liệu!");
    }
    autoResizeColumnWidth(tblBill);
}
    
    // set chiều rộng cột auto
public void autoResizeColumnWidth(JTable table) {
    final TableColumnModel columnModel = table.getColumnModel();
    for (int column = 0; column < table.getColumnCount(); column++) {
        int width = 50; // chiều rộng tối thiểu
        for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer renderer = table.getCellRenderer(row, column);
            Component comp = table.prepareRenderer(renderer, row, column);
            width = Math.max(comp.getPreferredSize().width + 10, width);
        }

        // So sánh với header
        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
        Component headerComp = headerRenderer.getTableCellRendererComponent(
            table, table.getColumnName(column), false, false, -1, column);
        width = Math.max(headerComp.getPreferredSize().width + 10, width);

        columnModel.getColumn(column).setPreferredWidth(width);
    }
}

public void selectTimeRange() {
    TimeRange range = TimeRange.today();
    switch (cboTimeRanges.getSelectedIndex()) {
        case 0 ->
            range = TimeRange.today();
        case 1 ->
            range = TimeRange.thisWeek();
        case 2 ->
            range = TimeRange.thisMonth();
        case 3 ->
            range = TimeRange.thisQuarter();
        case 4 ->
            range = TimeRange.thisYear();
    }
    jTextField1.setText(XDate.format(range.getBegin(), "dd/MM/yyyy"));
    txtEnd.setText(XDate.format(range.getEnd(), "dd/MM/yyyy"));
    this.fillTableBill();
}

    private void selectAll() {
        for (int i = 0; i < tblBill.getRowCount(); i++) {
            tblBill.setValueAt(true, i, 6); // Column 6 is checkbox column
        }
    }

    private void noSelectAll() {
        for (int i = 0; i < tblBill.getRowCount(); i++) {
            tblBill.setValueAt(false, i, 6);
        }
    }

    private void deleteSelectedBills() {
        if (MsgBox.confirm(this, "Bạn có chắc chắn muốn xóa các hóa đơn đã chọn?")) {
            boolean success = false;
            for (int i = tblBill.getRowCount() - 1; i >= 0; i--) {
                Object value = tblBill.getValueAt(i, 6); // Cột checkbox
                if (value != null && (Boolean) value) {
                    try {
                        Long id = Long.valueOf(tblBill.getValueAt(i, 0).toString()); // Cột chứa ID (kiểu Long)
                        daoBill.delete(id);
                        success = true;
                    } catch (Exception e) {
                        MsgBox.alert(this, "Lỗi: " + e.getMessage());
                        continue;
                    }
                }
            }
            if (success) {
                MsgBox.alert(this, "Xóa thành công!");
            } else {
                MsgBox.alert(this, "Vui lòng chọn hóa đơn cần xóa!");
            }
            fillTableBill(); // Cập nhật lại bảng
        }
    }

    private boolean validateBillForm() {
        String id = txtIDBill.getText().trim();
        String cardId = txtCardId.getText().trim();
        String checkin = txtCheckin.getText().trim();
        String checkout = txtCheckout.getText().trim();
        String username = txtUsername.getText().trim();

        if (id.isEmpty()) {
            MsgBox.alert(this, "Vui lòng nhập mã hóa đơn!");
            return false;
        }
        try {
            Long.parseLong(id);
        } catch (NumberFormatException e) {
            MsgBox.alert(this, "Mã hóa đơn phải là số!");
            return false;
        }

        if (cardId.isEmpty()) {
            MsgBox.alert(this, "Vui lòng nhập mã thẻ!");
            return false;
        }
        try {
            Integer.parseInt(cardId);
        } catch (NumberFormatException e) {
            MsgBox.alert(this, "Mã thẻ phải là số!");
            return false;
        }

        if (checkin.isEmpty()) {
            MsgBox.alert(this, "Vui lòng nhập thời điểm tạo!");
            return false;
        }
        if (checkout.isEmpty()) {
            MsgBox.alert(this, "Vui lòng nhập thời điểm thanh toán!");
            return false;
        }

        // Kiểm tra định dạng ngày giờ
        try {
            XDate.parse(checkin, "HH:mm:ss dd-MM-yyyy");
            XDate.parse(checkout, "HH:mm:ss dd-MM-yyyy");
        } catch (Exception e) {
            MsgBox.alert(this, "Thời gian phải đúng định dạng 'HH:mm:ss dd-MM-yyyy'!");
            return false;
        }

        if (username.isEmpty()) {
            MsgBox.alert(this, "Vui lòng nhập người tạo!");
            return false;
        }

        if (!rdoServicing.isSelected() && !rdoCompleted.isSelected() && !rdoCanceled.isSelected()) {
            MsgBox.alert(this, "Vui lòng chọn trạng thái!");
            return false;
        }

        return true;
    }

    public Bill getForm() {
        Bill b = new Bill();
        if (!txtIDBill.getText().trim().isEmpty()) {
            b.setId(Long.valueOf(txtIDBill.getText().trim()));
        }
        b.setUsername(txtUsername.getText().trim());
        b.setCardId(Integer.valueOf(txtCardId.getText().trim()));
        b.setCheckin(XDate.parse(txtCheckin.getText(), "HH:mm:ss dd-MM-yyyy"));
        b.setCheckout(XDate.parse(txtCheckout.getText(), "HH:mm:ss dd-MM-yyyy"));

        if (rdoServicing.isSelected()) {
            b.setStatus(0);
        } else if (rdoCompleted.isSelected()) {
            b.setStatus(1);
        } else {
            b.setStatus(2);
        }
        return b;
    }
    
    void setForm(Bill b) {
        txtIDBill.setText(String.valueOf(b.getId()));
        txtUsername.setText(b.getUsername());
        txtCardId.setText(String.valueOf(b.getCardId()));
        txtCheckin.setText(XDate.format(b.getCheckin(), "HH:mm:ss dd-MM-yyyy"));
        txtCheckout.setText(XDate.format(b.getCheckout(), "HH:mm:ss dd-MM-yyyy"));
        
        switch (b.getStatus()) {
            case 0 -> rdoServicing.setSelected(true);
            case 1 -> rdoCompleted.setSelected(true);
            default -> rdoCanceled.setSelected(true);
        }
    }
    
    void clearForm() {
        txtIDBill.setText("");
        txtUsername.setText("");
        txtCardId.setText("");
        txtCheckin.setText("");
        txtCheckout.setText("");
        rdoServicing.setSelected(true);
        row = -1;
        this.setEditable(false);
    }
    
    void edit() {
        row = tblBill.getSelectedRow();
        if (row < 0) {
            MsgBox.alert(this, "Vui lòng chọn hóa đơn để xem chi tiết.");
            return;
        }

        Long billId = (Long) tblBill.getValueAt(row, 0); // lấy ID phiếu
        Bill bill = daoBill.selectByID(billId);

        if (bill != null) {
            txtIDBill.setText(String.valueOf(bill.getId()));
            txtCardId.setText(String.valueOf(bill.getCardId()));
            txtCheckin.setText(XDate.format(bill.getCheckin(), "HH:mm:ss dd-MM-yyyy"));
            txtCheckout.setText(XDate.format(bill.getCheckout(), "HH:mm:ss dd-MM-yyyy"));
            txtUsername.setText(bill.getUsername());

            // Trạng thái
            switch (bill.getStatus()) {
                case 0 -> rdoServicing.setSelected(true);
                case 1 -> rdoCompleted.setSelected(true);
                case 2 -> rdoCanceled.setSelected(true);
            }

            fillTableBillDetails(billId); // Bổ sung bảng chi tiết
            this.setEditable(true); 
            Tabs.setSelectedIndex(1);
        } else {
            MsgBox.alert(this, "Không tìm thấy dữ liệu hóa đơn.");
        }
    }
    
    void fillTableBillDetails(Long billId) {
        DefaultTableModel model = (DefaultTableModel) tblBillDetail.getModel();
        model.setRowCount(0);

        try {
            List<BillDetail> list = daoDetail.selectBillId_lab4(billId);
            for (BillDetail bd : list) {
                Object[] row = {
                    bd.getDrinkId(),
                    bd.getDrinkName(),
                    bd.getUnitPrice(),
                    bd.getDiscount(),
                    bd.getQuantity(),
                    bd.getQuantity() * bd.getUnitPrice() * (1 - bd.getDiscount())
                };
                model.addRow(row);
            }
            autoResizeColumnWidth(tblBillDetail);
        } catch (Exception e) {
            MsgBox.alert(this, "Lỗi khi tải chi tiết hóa đơn!");
            e.printStackTrace();
        }
    }
    
    void insert() {
        // Bước 1: Xác thực form đầu vào
        if (!validateBillForm()) {
            return;
        }

        // Bước 2: Lấy dữ liệu từ form (sau khi đã xác thực)
        Bill b = getForm();
        if (b == null) {
            return; // getForm() báo lỗi và trả về null (ví dụ lỗi parse date hoặc mã thẻ rỗng)
        }

        // Bước 3: Thêm vào DB
        try {
            daoBill.insert(b);
            fillTableBill();
            clearForm();
            MsgBox.alert(this, "Thêm hóa đơn thành công");
        } catch (Exception e) {
            MsgBox.alert(this, "Lỗi thêm hóa đơn: " + e.getMessage());
        }
    }
    
    void update() {
        // Kiểm tra form đầu vào trước
        if (!validateBillForm()) {
            return;
        }

        Bill b = getForm();
        if (b == null || b.getId() == null) {
            MsgBox.alert(this, "Vui lòng chọn hóa đơn hợp lệ để cập nhật");
            return;
        }

        try {
            daoBill.update(b);
            fillTableBill();
            clearForm();
            MsgBox.alert(this, "Cập nhật hóa đơn thành công");
        } catch (Exception e) {
            MsgBox.alert(this, "Lỗi cập nhật: " + e.getMessage());
        }
    }
    
    void delete() {
        String idText = txtIDBill.getText().trim();

        if (idText.isEmpty()) {
            MsgBox.alert(this, "Vui lòng nhập mã hóa đơn để xoá");
            return;
        }

        Long id = null;
        try {
            id = Long.valueOf(idText);
        } catch (NumberFormatException e) {
            MsgBox.alert(this, "Mã hóa đơn không hợp lệ. Vui lòng nhập số.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa hóa đơn này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            daoBill.delete(id);
            fillTableBill();
            clearForm();
            MsgBox.alert(this, "Xóa hóa đơn thành công");
        } catch (Exception e) {
            MsgBox.alert(this, "Lỗi xóa: " + e.getMessage());
        }
    }

    private void clearTableSelectionAndEditor() {
        TableCellEditor editor = tblBill.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }
        tblBill.clearSelection();
        this.requestFocusInWindow();
    }
    
    void setEditable(boolean editable) {
    txtIDBill.setEnabled(!editable);     // Mã chỉ nhập khi thêm mới
    btnAdd.setEnabled(!editable);         // Tạo mới chỉ khi không trong chế độ sửa
    btnUpdate.setEnabled(editable);          // Chỉ bật khi đang sửa
    btnDelete.setEnabled(editable);          // Chỉ bật khi đang sửa
    
    int rowCount = tblBill.getRowCount();
    btnMoveFirst.setEnabled(editable && rowCount > 0);
    btnMovePrevious.setEnabled(editable && rowCount > 0);
    btnMoveNext.setEnabled(editable && rowCount > 0);
    btnMoveLast.setEnabled(editable && rowCount > 0);

}
    void moveFirst() {
    moveTo(0);
}

void movePrevious() {
    int index = tblBill.getSelectedRow() - 1;
    moveTo(index);
}

void moveNext() {
    int index = tblBill.getSelectedRow() + 1;
    moveTo(index);
}

void moveLast() {
    int lastIndex = tblBill.getRowCount() - 1;
    moveTo(lastIndex);
}

void moveTo(int index) {
    int rowCount = tblBill.getRowCount();

    if (rowCount == 0) return;

    if (index < 0) {
        moveLast();
    } else if (index >= rowCount) {
        moveFirst();
    } else {
        tblBill.clearSelection();
        tblBill.setRowSelectionInterval(index, index);
        edit();
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        Tabs = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtEnd = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        cboTimeRanges = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBill = new javax.swing.JTable();
        btnSelectAll = new javax.swing.JButton();
        btnNoSelect = new javax.swing.JButton();
        btnDeleteSelect = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtIDBill = new javax.swing.JTextField();
        txtCheckin = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtCardId = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtCheckout = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        rdoServicing = new javax.swing.JRadioButton();
        rdoCompleted = new javax.swing.JRadioButton();
        rdoCanceled = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblBillDetail = new javax.swing.JTable();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnMoveFirst = new javax.swing.JButton();
        btnMovePrevious = new javax.swing.JButton();
        btnMoveNext = new javax.swing.JButton();
        btnMoveLast = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        Tabs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabsMouseClicked(evt);
            }
        });

        jLabel1.setText("Từ ngày: ");

        jTextField1.setText("01/01/2025");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel3.setText("Đến ngày:");

        txtEnd.setText("01/01/2026");
        txtEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEndActionPerformed(evt);
            }
        });

        btnSearch.setText("Lọc");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        cboTimeRanges.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Năm nay", "Hôm nay", "Tuần này", "Tháng này", "Quý này", " " }));
        cboTimeRanges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTimeRangesActionPerformed(evt);
            }
        });

        tblBill.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Mã phiếu", "Thẻ số", "Thời điểm tạo", "Thời điểm thanh toán", "Trạng thái", "Email", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblBill.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tblBillMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(tblBill);

        btnSelectAll.setText("Chọn tất cả");
        btnSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectAllActionPerformed(evt);
            }
        });

        btnNoSelect.setText("Bỏ chọn tất cả");
        btnNoSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNoSelectActionPerformed(evt);
            }
        });

        btnDeleteSelect.setText("Xoá các mục chọn");
        btnDeleteSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteSelectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(75, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(txtEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(cboTimeRanges, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 616, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSelectAll)
                .addGap(18, 18, 18)
                .addComponent(btnNoSelect)
                .addGap(18, 18, 18)
                .addComponent(btnDeleteSelect)
                .addGap(13, 13, 13))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(txtEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch)
                    .addComponent(cboTimeRanges, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSelectAll)
                    .addComponent(btnNoSelect)
                    .addComponent(btnDeleteSelect))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        Tabs.addTab("DANH SÁCH", jPanel1);

        jLabel2.setText("Mã phiếu");

        jLabel4.setText("Thời điểm tạo");

        jLabel5.setText("Thẻ số");

        jLabel6.setText("Thời điểm thanh toán");

        jLabel7.setText("Trạng thái");

        buttonGroup1.add(rdoServicing);
        rdoServicing.setText("Servivcing");

        buttonGroup1.add(rdoCompleted);
        rdoCompleted.setText("Completed");

        buttonGroup1.add(rdoCanceled);
        rdoCanceled.setText("Canceled");

        jLabel8.setText("Người tạo");

        jLabel9.setText("Phiếu chi tiết");

        tblBillDetail.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Đồ uống", "Đơn giá", "Giảm giá", "Số lượng", "Thành tiền"
            }
        ));
        jScrollPane2.setViewportView(tblBillDetail);

        btnAdd.setText("Tạo mới");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnUpdate.setText("Cập nhật");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnDelete.setText("Xoá");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnClear.setText("Nhập mới");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnMoveFirst.setText("|<");
        btnMoveFirst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveFirstActionPerformed(evt);
            }
        });

        btnMovePrevious.setText("<<");
        btnMovePrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMovePreviousActionPerformed(evt);
            }
        });

        btnMoveNext.setText(">>");
        btnMoveNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveNextActionPerformed(evt);
            }
        });

        btnMoveLast.setText(">|");
        btnMoveLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveLastActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtCheckin)
                                            .addComponent(txtIDBill)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel2)
                                                    .addComponent(jLabel4))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                        .addGap(18, 18, 18))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(rdoServicing, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(rdoCompleted, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(rdoCanceled, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jLabel7))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel8)
                                    .addComponent(txtUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                                    .addComponent(txtCheckout)
                                    .addComponent(txtCardId))))
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnUpdate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnClear)
                        .addGap(44, 44, 44)
                        .addComponent(btnMoveFirst, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMovePrevious, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMoveNext, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMoveLast, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jSeparator1)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtIDBill, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCardId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(23, 23, 23)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCheckin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCheckout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(29, 29, 29)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdoServicing)
                    .addComponent(rdoCompleted)
                    .addComponent(rdoCanceled)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnUpdate)
                    .addComponent(btnDelete)
                    .addComponent(btnClear)
                    .addComponent(btnMoveFirst)
                    .addComponent(btnMovePrevious)
                    .addComponent(btnMoveNext)
                    .addComponent(btnMoveLast))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        Tabs.addTab("BIỂU MẪU", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 6, Short.MAX_VALUE)
                .addComponent(Tabs, javax.swing.GroupLayout.PREFERRED_SIZE, 649, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Tabs, javax.swing.GroupLayout.PREFERRED_SIZE, 503, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cboTimeRangesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTimeRangesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboTimeRangesActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void txtEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEndActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtEndActionPerformed

    private void btnSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllActionPerformed
        // TODO add your handling code here:
        selectAll();
    }//GEN-LAST:event_btnSelectAllActionPerformed

    private void btnNoSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNoSelectActionPerformed
        // TODO add your handling code here:
        noSelectAll();
    }//GEN-LAST:event_btnNoSelectActionPerformed

    private void btnDeleteSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteSelectActionPerformed
        // TODO add your handling code here:
        deleteSelectedBills();
    }//GEN-LAST:event_btnDeleteSelectActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
        insert();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        // TODO add your handling code here:
        update();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
        delete();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        // TODO add your handling code here:
        clearForm();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnMoveFirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveFirstActionPerformed
        // TODO add your handling code here:
        this.moveFirst();
    }//GEN-LAST:event_btnMoveFirstActionPerformed

    private void btnMovePreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMovePreviousActionPerformed
        // TODO add your handling code here:
        this.movePrevious();
    }//GEN-LAST:event_btnMovePreviousActionPerformed

    private void btnMoveNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveNextActionPerformed
        // TODO add your handling code here:
        this.moveNext();
    }//GEN-LAST:event_btnMoveNextActionPerformed

    private void btnMoveLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveLastActionPerformed
        // TODO add your handling code here:
        this.moveLast();
    }//GEN-LAST:event_btnMoveLastActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
        if (!validateDate()) {
            return;
        }
        fillTableBill();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void TabsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabsMouseClicked
        // TODO add your handling code here:
        clearTableSelectionAndEditor();
    }//GEN-LAST:event_TabsMouseClicked

    private void tblBillMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBillMousePressed
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            edit();
        }
    }//GEN-LAST:event_tblBillMousePressed

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
            java.util.logging.Logger.getLogger(BillManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BillManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BillManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BillManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                BillManagerJDialog dialog = new BillManagerJDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JTabbedPane Tabs;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnDeleteSelect;
    private javax.swing.JButton btnMoveFirst;
    private javax.swing.JButton btnMoveLast;
    private javax.swing.JButton btnMoveNext;
    private javax.swing.JButton btnMovePrevious;
    private javax.swing.JButton btnNoSelect;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSelectAll;
    private javax.swing.JButton btnUpdate;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cboTimeRanges;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JRadioButton rdoCanceled;
    private javax.swing.JRadioButton rdoCompleted;
    private javax.swing.JRadioButton rdoServicing;
    private javax.swing.JTable tblBill;
    private javax.swing.JTable tblBillDetail;
    private javax.swing.JTextField txtCardId;
    private javax.swing.JTextField txtCheckin;
    private javax.swing.JTextField txtCheckout;
    private javax.swing.JTextField txtEnd;
    private javax.swing.JTextField txtIDBill;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
