/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package poly.cafe.ui;

import java.awt.Image;
import java.io.File;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import poly.cafe.dao.UserDAO;
import poly.cafe.entity.User;
import poly.cafe.util.MsgBox;
import poly.cafe.util.XImage;

/**
 *
 * @author acer
 */
public class UserManagerJDialog extends javax.swing.JDialog {
    UserDAO dao = new UserDAO();
    int row;
    
    void init() {
        setIconImage(XImage.getAppIcon());
        setLocationRelativeTo(null);
        setTitle("DANH MỤC USER");
        fillTable();
        clearForm();
    }
    /**
     * Creates new form UserManagerJDialog
     */
    public UserManagerJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        lblPhoto.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                chooseImage(); // gọi hàm chọn ảnh
            }
        });
        init();
    }
    
    void fillTable() {
    DefaultTableModel model = (DefaultTableModel) tblUser.getModel();
    model.setRowCount(0);
    try {
        List<User> list = dao.selectAll();
        for (User user : list) {
            Object[] row = {
                user.getUsername(),
                user.getPassword(),
                user.getFullname(),
                user.getPhoto(),
                user.isManager() ? "Quản lý" : "Nhân viên",
                user.isEnabled() ? "Hoạt động" : "Tạm dừng",
                false // checkbox để chọn xoá
            };
            model.addRow(row);
        }
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi truy vấn dữ liệu!");
    }
}

    
private void selectAll() {
    for (int i = 0; i < tblUser.getRowCount(); i++) {
        tblUser.setValueAt(true, i, 6);
    }
}

private void noSelectAll() {
    for (int i = 0; i < tblUser.getRowCount(); i++) {
        tblUser.setValueAt(false, i, 6);
    }
}
    public void deleteUser() {
    if (MsgBox.confirm(this, "Bạn đang thực hiện chức năng xoá")) {
        boolean kq = false;
        for (int i = tblUser.getRowCount() - 1; i >= 0; i--) {
            Object isChecked = tblUser.getValueAt(i, 6); // Cột checkbox
            if (isChecked != null && (Boolean) isChecked) {
                String id = tblUser.getValueAt(i, 0).toString(); // Cột tên đăng nhập
                try {
                    dao.delete(id);
                    kq = true;
                } catch (Exception e) {
                    MsgBox.alert(this, "Lỗi: " + e.getMessage());
                }
            }
        }
        if (kq) {
            MsgBox.alert(this, "Xoá thành công");
        } else {
            MsgBox.alert(this, "Vui lòng chọn dòng cần xoá");
        }
        this.fillTable();
    }
}
    public boolean ValidateForm() {
    String username = txtUsername.getText().trim();
    String password = new String(txtPassword.getPassword()).trim();
    String confirm = new String(txtConfirmPassword.getPassword()).trim();
    String fullname = txtFullname.getText().trim();

    if (username.isEmpty()) {
        MsgBox.alert(this, "Vui lòng nhập tên đăng nhập!");
        return false;
    }
    if (password.isEmpty()) {
        MsgBox.alert(this, "Vui lòng nhập mật khẩu!");
        return false;
    }
    if (!password.equals(confirm)) {
        MsgBox.alert(this, "Xác nhận mật khẩu không khớp!");
        return false;
    }
    if (fullname.isEmpty()) {
        MsgBox.alert(this, "Vui lòng nhập họ và tên!");
        return false;
    }

    return true;
}

        
    private void chooseImage() {
    JFileChooser fileChooser = new JFileChooser();
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        // Copy ảnh vào thư mục /logos
        XImage.save(file); 
        // Hiển thị ảnh
        ImageIcon icon = XImage.read(file.getName());
        lblPhoto.setIcon(new ImageIcon(icon.getImage().getScaledInstance(lblPhoto.getWidth(), lblPhoto.getHeight(), Image.SCALE_SMOOTH)));
        lblPhoto.setToolTipText(file.getName()); // Ghi nhớ tên file ảnh
    }
}
public User getForm() {
    String username = txtUsername.getText().trim();
    String password = new String(txtPassword.getPassword()).trim();
    String fullname = txtFullname.getText().trim();
    boolean manager = rdoManager.isSelected();
    boolean enabled = rdoEnabled.isSelected();

    return User.builder()
            .photo(lblPhoto.getToolTipText())
            .username(username)
            .password(password)
            .fullname(fullname)
            .manager(manager)
            .enabled(enabled)
            .build();
}

public void setForm(User user) {
    if (user.getPhoto() != null) {
    ImageIcon icon = XImage.read(user.getPhoto());
    lblPhoto.setIcon(new ImageIcon(icon.getImage().getScaledInstance(lblPhoto.getWidth(), lblPhoto.getHeight(), Image.SCALE_SMOOTH)));
    lblPhoto.setToolTipText(user.getPhoto());
    } else {
        lblPhoto.setIcon(null);
        lblPhoto.setToolTipText(null);
    }
    txtUsername.setText(user.getUsername());
    txtPassword.setText(user.getPassword());
    txtConfirmPassword.setText(user.getPassword());
    txtFullname.setText(user.getFullname());
    rdoManager.setSelected(user.isManager());
    rdoStaff.setSelected(!user.isManager());
    rdoEnabled.setSelected(user.isEnabled());
    rdoDisabled.setSelected(!user.isEnabled());
}

public void clearForm() {
    lblPhoto.setIcon(null);
    lblPhoto.setToolTipText(null);
    txtUsername.setText("");
    txtPassword.setText("");
    txtConfirmPassword.setText("");
    txtFullname.setText("");
    rdoManager.setSelected(true);
    rdoEnabled.setSelected(true);
    row = -1;
    this.setEditable(false);
}

private void clearTableSelectionAndEditor() {
    TableCellEditor editor = tblUser.getCellEditor();
    if (editor != null) {
        editor.stopCellEditing();
    }
    tblUser.clearSelection();
    this.requestFocusInWindow();
}

public void edit() {
    row = tblUser.getSelectedRow();
    if (row >= 0) {
        String username = tblUser.getValueAt(row, 0).toString();
        User user = dao.selectByID(username);
        if (user != null) {
            setForm(user);
            Tabs.setSelectedIndex(1);
            this.setEditable(true);
            clearTableSelectionAndEditor();
        } else {
            MsgBox.alert(this, "Không tìm thấy người dùng!");
        }
    } else {
        MsgBox.alert(this, "Vui lòng chọn người dùng để sửa!");
    }
}
    public void insert() {
    if (!ValidateForm()) return;

    User user = getForm();
    if (dao.selectByID(user.getUsername()) != null) {
        MsgBox.alert(this, "Tên đăng nhập đã tồn tại!");
        txtUsername.requestFocus();
        return;
    }

    try {
        dao.insert(user);
        fillTable();
        clearForm();
        MsgBox.alert(this, "Thêm người dùng thành công!");
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi khi thêm: " + e.getMessage());
    }
}

    public void update() {
    if (!ValidateForm()) return;

    User user = getForm();
    if (dao.selectByID(user.getUsername()) == null) {
        MsgBox.alert(this, "Không tồn tại tên đăng nhập để cập nhật!");
        return;
    }

    try {
        dao.update(user);
        fillTable();
        clearForm();
        MsgBox.alert(this, "Cập nhật người dùng thành công!");
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi khi cập nhật: " + e.getMessage());
    }
}

    public void delete() {
    String username = txtUsername.getText().trim();
    if (username.isEmpty()) {
        MsgBox.alert(this, "Vui lòng nhập tên đăng nhập để xoá!");
        return;
    }

    if (dao.selectByID(username) == null) {
        MsgBox.alert(this, "Không tồn tại tên đăng nhập!");
        return;
    }

    try {
        dao.delete(username);
        fillTable();
        clearForm();
        MsgBox.alert(this, "Xoá người dùng thành công!");
    } catch (Exception e) {
        MsgBox.alert(this, "Lỗi khi xoá: " + e.getMessage());
    }
}
    void setEditable(boolean editable) {
    txtUsername.setEnabled(!editable);     // Mã chỉ nhập khi thêm mới
    btnAdd.setEnabled(!editable);         // Tạo mới chỉ khi không trong chế độ sửa
    btnUpdate.setEnabled(editable);          // Chỉ bật khi đang sửa
    btnDelete.setEnabled(editable);          // Chỉ bật khi đang sửa
    
    int rowCount = tblUser.getRowCount();
    btnMoveFirst.setEnabled(editable && rowCount > 0);
    btnMovePrevious.setEnabled(editable && rowCount > 0);
    btnMoveNext.setEnabled(editable && rowCount > 0);
    btnMoveLast.setEnabled(editable && rowCount > 0);
}
    void moveFirst() {
    moveTo(0);
}

void movePrevious() {
    int index = tblUser.getSelectedRow() - 1;
    moveTo(index);
}

void moveNext() {
    int index = tblUser.getSelectedRow() + 1;
    moveTo(index);
}

void moveLast() {
    int lastIndex = tblUser.getRowCount() - 1;
    moveTo(lastIndex);
}

void moveTo(int index) {
    int rowCount = tblUser.getRowCount();

    if (rowCount == 0) return;

    if (index < 0) {
        moveLast();
    } else if (index >= rowCount) {
        moveFirst();
    } else {
        tblUser.clearSelection();
        tblUser.setRowSelectionInterval(index, index);
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
        buttonGroup2 = new javax.swing.ButtonGroup();
        Tabs = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUser = new javax.swing.JTable();
        btnSelectAll = new javax.swing.JButton();
        btnNoSelect = new javax.swing.JButton();
        btnDeleteSelect = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnMoveFirst = new javax.swing.JButton();
        btnMovePrevious = new javax.swing.JButton();
        btnMoveNext = new javax.swing.JButton();
        btnMoveLast = new javax.swing.JButton();
        lblPhoto = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtFullname = new javax.swing.JTextField();
        txtUsername = new javax.swing.JTextField();
        rdoStaff = new javax.swing.JRadioButton();
        rdoManager = new javax.swing.JRadioButton();
        rdoEnabled = new javax.swing.JRadioButton();
        rdoDisabled = new javax.swing.JRadioButton();
        txtPassword = new javax.swing.JPasswordField();
        txtConfirmPassword = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        Tabs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabsMouseClicked(evt);
            }
        });

        tblUser.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Tên đăng nhập", "Mật khẩu", "Họ và tên", "Hình ảnh", "Vai trò", "Trạng thái", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tblUser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblUserMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tblUserMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(tblUser);

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

        btnDeleteSelect.setText("Xóa các mục chọn");
        btnDeleteSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteSelectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnSelectAll)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNoSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDeleteSelect)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSelectAll)
                    .addComponent(btnNoSelect)
                    .addComponent(btnDeleteSelect))
                .addGap(14, 14, 14))
        );

        Tabs.addTab("DANH SÁCH", jPanel1);

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

        btnDelete.setText("Xóa");
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

        lblPhoto.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 0, 0)));

        jLabel2.setText("Tên đăng nhập");

        jLabel3.setText("Họ và tên");

        jLabel4.setText("Mật khẩu");

        jLabel5.setText("Xác nhận mật khẩu");

        jLabel6.setText("Vai trò");

        jLabel7.setText("Trạng thái");

        txtFullname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFullnameActionPerformed(evt);
            }
        });

        buttonGroup1.add(rdoStaff);
        rdoStaff.setText("Nhân viên ");

        buttonGroup1.add(rdoManager);
        rdoManager.setText("Quản lý");

        buttonGroup2.add(rdoEnabled);
        rdoEnabled.setText("Hoạt động");

        buttonGroup2.add(rdoDisabled);
        rdoDisabled.setText("Tạm dừng");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnUpdate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnDelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnClear)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 116, Short.MAX_VALUE)
                        .addComponent(btnMoveFirst, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMovePrevious, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMoveNext, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMoveLast, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(lblPhoto, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(rdoManager)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rdoStaff))
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(txtPassword))
                        .addGap(26, 26, 26)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel5)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFullname, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(rdoEnabled)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rdoDisabled))
                            .addComponent(txtConfirmPassword))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPhoto, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFullname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addGap(8, 8, 8)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addGap(17, 17, 17)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rdoManager)
                            .addComponent(rdoStaff)
                            .addComponent(rdoEnabled)
                            .addComponent(rdoDisabled))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnMoveLast)
                            .addComponent(btnMoveNext)
                            .addComponent(btnMovePrevious)
                            .addComponent(btnMoveFirst)
                            .addComponent(btnClear)
                            .addComponent(btnDelete)
                            .addComponent(btnUpdate)
                            .addComponent(btnAdd))
                        .addContainerGap())))
        );

        Tabs.addTab("BIỂU MẪU", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Tabs)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Tabs, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tblUserMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblUserMousePressed
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            edit();
        }
    }//GEN-LAST:event_tblUserMousePressed

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
        deleteUser();
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

    private void TabsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabsMouseClicked
        // TODO add your handling code here:
        clearTableSelectionAndEditor();
    }//GEN-LAST:event_TabsMouseClicked

    private void txtFullnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFullnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFullnameActionPerformed

    private void tblUserMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblUserMouseClicked
        // TODO add your handling code here:
        
    }//GEN-LAST:event_tblUserMouseClicked

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
            java.util.logging.Logger.getLogger(UserManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UserManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UserManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UserManagerJDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UserManagerJDialog dialog = new UserManagerJDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnSelectAll;
    private javax.swing.JButton btnUpdate;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblPhoto;
    private javax.swing.JRadioButton rdoDisabled;
    private javax.swing.JRadioButton rdoEnabled;
    private javax.swing.JRadioButton rdoManager;
    private javax.swing.JRadioButton rdoStaff;
    private javax.swing.JTable tblUser;
    private javax.swing.JPasswordField txtConfirmPassword;
    private javax.swing.JTextField txtFullname;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
