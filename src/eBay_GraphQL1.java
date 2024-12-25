import javax.swing.*;
import java.awt.*;
import java.io.StringReader;
import java.net.http.*;
import java.net.URI;
import java.util.logging.*;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class eBay_GraphQL1 {
    private static final String API_URL = "https://api.ebay.com/ws/api.dll";
    private static final String OAUTH_TOKEN = "v^1.1#i^1#f^0#p^3#I^3#r^1#t^Ul4xMF83OkZCODQxMjhDOUQ2NjhBODU2MDcwMjFDQUVEOThFNzA5XzBfMSNFXjI2MA==";// 替换为实际的 Token
    private static final String OAUTHFIX_TOKEN = "v^1.1#i^1#f^0#r^1#p^3#I^3#t^Ul4xMF82OjEyQjVBMjhFMUZFNzc4MzU2QzI0NkEyQUU5NzVGRjU0XzBfMSNFXjI2MA=="; // 替换为实际修改价格的 Token
    private static final String GRAPHQL_URL = "http://api.epms.leyinlin.com/v1beta1/relay"; // GraphQL 接口地址
    private static final String GRAPHQL_ADMIN_SECRET = "myadminsecretkey"; // 替换为实际的 Secret 密钥
    private static final Logger logger = Logger.getLogger(eBay_GraphQL1.class.getName());// 创建一个名为logger的日志记录器实例，用于记录日志信息



    public static void main(String[] args) {
        setupLogger(); // 初始化日志记录器，输出日志到文件和控制台

        // UI 初始化
        JFrame frame = new JFrame("eBay & GraphQL API 监控客户端");// 创建一个JFrame窗口，标题为"eBay & GraphQL API 监控客户端"
        frame.pack(); // 自动调整窗口大小以适应内容
        frame.setMinimumSize(new Dimension(1500, 900)); // 设置最小尺寸
        frame.setLocationRelativeTo(null); // 窗口居中显示
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 设置窗口的默认关闭操作为退出程序
        frame.setLayout(new BorderLayout(10, 10)); // 使用 BorderLayout 并设置组件间距

        // 字体设置
        // 创建一个字体对象，字体名称为"微软雅黑"，样式为正常，大小为14
        Font font = new Font("微软雅黑", Font.PLAIN, 14);
        // 将标签的字体设置为上面创建的font对象
        UIManager.put("Label.font", font);
        // 将按钮的字体设置为上面创建的font对象
        UIManager.put("Button.font", font);
        // 将文本框的字体设置为上面创建的font对象
        UIManager.put("TextField.font", font);
        // 将文本区域的字体设置为上面创建的font对象
        UIManager.put("TextArea.font", font);
        // 将组合框的字体设置为上面创建的font对象
        UIManager.put("ComboBox.font", font);


        // 输入部分
        // 创建一个标签，显示文本"eBay 的商品 ItemID:"
        JLabel inputLabel = new JLabel("eBay 的商品 ItemID:");
        // 创建一个文本输入框，宽度为20列
        JTextField inputField = new JTextField(20);
        // 设置文本框的默认文本为"375818151669"
        inputField.setText("375818151669");

        // 创建一个按钮，显示文本"获取 eBay 商品信息"
        JButton fetchButton = new JButton("获取 eBay 商品信息");
        // 创建一个按钮，显示文本"获取 GraphQL 数据"
        JButton fetchGraphQLButton = new JButton("获取 GraphQL 数据");
        // 创建一个下拉框，类型为字符串
        JComboBox<String> skuComboBox = new JComboBox<>();
        // 向下拉框中添加一个初始选项"选择 SKU"
        skuComboBox.addItem("选择 SKU");


        // eBay 结果框
        JTextArea ebayResultArea = new JTextArea(10, 50);// 创建一个文本区域，行数为10，列数为50
        ebayResultArea.setEditable(false);// 设置文本区域为不可编辑
        JScrollPane ebayScrollPane = new JScrollPane(ebayResultArea);// 创建一个滚动面板，将文本区域嵌入到滚动面板中

        // GraphQL 结果框
        JTextArea graphqlResultArea = new JTextArea(10, 50);// 创建一个文本区域，行数为10，列数为50
        graphqlResultArea.setEditable(false);// 设置文本区域为不可编辑
        JScrollPane graphqlScrollPane = new JScrollPane(graphqlResultArea);// 创建一个滚动面板，将文本区域放入其中，以便于滚动查看内容

        // 日志区域
        JTextArea logArea = new JTextArea(8, 50);
        logArea.setEditable(false);

        // 输入区域面板
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("输入区域")); // 添加面板标题
        GridBagConstraints gbc = new GridBagConstraints();// 创建一个面板，使用网格包布局管理器
        gbc.insets = new Insets(5, 5, 5, 5); // 设置组件之间的间距
        gbc.fill = GridBagConstraints.HORIZONTAL;// 创建一个网格约束对象，用于设置组件在网格中的位置和大小

        // 添加 eBay 的父商品 ID 输入框
        // 设置GridBagConstraints的网格位置为(0, 0)
        // 这将把组件放置在输入面板的第一列和第一行
        gbc.gridx = 0;
        gbc.gridy = 0;
        // 创建一个标签，显示文本"eBay 的商品 ItemID:"，并将其添加到输入面板中
        inputPanel.add(new JLabel("eBay 的商品 ItemID:"), gbc);
        // 将GridBagConstraints的列索引设置为1，准备将下一个组件放置在第二列
        gbc.gridx = 1;
        // 添加之前创建的文本框（inputField）到输入面板中
        inputPanel.add(inputField, gbc);

        // 添加 SKU 下拉框
        gbc.gridx = 0;// 设置网格约束对象的x坐标为0，表示该组件在网格中的列位置
        gbc.gridy = 1;// 设置网格约束对象的y坐标为1，表示该组件在网格中的行位置
        inputPanel.add(new JLabel("SKU:"), gbc);// 将标签“SKU:”添加到输入面板中，使用当前的网格约束
        gbc.gridx = 1;// 设置网格约束对象的x坐标为1，表示下一个组件在网格中的列位置
        inputPanel.add(skuComboBox, gbc);// 将SKU下拉框添加到输入面板中，使用当前的网格约束


        JTextArea graphqlQueryArea = new JTextArea(4, 50);// 创建一个文本区域，行数为4，列数为50
        graphqlQueryArea.setLineWrap(true);// 设置文本区域的自动换行为启用
        graphqlQueryArea.setWrapStyleWord(true);// 设置文本区域的换行方式为按单词换行

        // 按钮行下移
        // 重置网格约束对象的宽度为1，表示该组件占用1个网格单元
        gbc.gridwidth = 1;
        // 设置网格约束对象的x坐标为0，表示该组件在网格中的列位置
        gbc.gridx = 0;
        // 设置网格约束对象的y坐标为4，表示该组件在网格中的行位置
        gbc.gridy = 4;
        // 将获取按钮添加到输入面板中，使用当前的网格约束
        inputPanel.add(fetchButton, gbc);
        // 设置网格约束对象的x坐标为1，表示下一个组件在网格中的列位置
        gbc.gridx = 1;
        // 将获取GraphQL按钮添加到输入面板中，使用当前的网格约束
        inputPanel.add(fetchGraphQLButton, gbc);



        // 添加一个新按钮用于修改价格
        JButton updatePricesButton = new JButton("修改价格");

        // 添加 ItemID 输入框
        JLabel itemIdLabel = new JLabel("eBay 的ALL IN Domain商品 ItemID:");
        JTextField itemIdField = new JTextField(30); // 调整输入框的宽度

        // 添加上传按钮
        JButton uploadToEbayButton = new JButton("上传到 eBay");

        // 添加到输入面板
        inputPanel.add(itemIdLabel);// 将 itemIdLabel 添加到输入面板，显示项目 ID 的标签
        inputPanel.add(itemIdField);// 将 itemIdField 添加到输入面板，用于用户输入项目 ID 的文本字段
        inputPanel.add(uploadToEbayButton);// 将 uploadToEbayButton 添加到输入面板，用于上传项目到 eBay 的按钮

        // 将按钮添加到输入面板
        // 设置按钮位置在现有按钮的下方
        // 假设输入面板使用 GridBagLayout 布局
        GridBagConstraints gbcUpdateButton = new GridBagConstraints();
        gbcUpdateButton.insets = new Insets(5, 5, 5, 5); // 设置内边距
        gbcUpdateButton.fill = GridBagConstraints.HORIZONTAL; // 设置填充方式
        gbcUpdateButton.gridx = 0; // 设置按钮位置为第 0 列
        gbcUpdateButton.gridy = 5; // 设置按钮位置为第 5 行
        gbcUpdateButton.gridwidth = 2; // 按钮跨两列
        inputPanel.add(updatePricesButton, gbcUpdateButton); // 将按钮添加到输入面板

// 定义一个规则类，用于存储价格计算规则
        class Rule {
            public double divisor; // 除数，用于某种计算
            public double additionalConstant; // 额外常量,可能是用于偏移或调整的固定值
            public double multiplier; // 乘数，用于计算时的系数

            // 构造函数，用于初始化规则参数
            public Rule(double divisor, double additionalConstant, double multiplier) {
                this.divisor = divisor;// 初始化除数
                this.additionalConstant = additionalConstant;// 初始化额外常量
                this.multiplier = multiplier; // 初始化乘数
            }
        }

        // 定义价格计算规则
        Rule priceRule = new Rule(7.21, 27, 0.9265); // 替换为实际规则参数


        // 结果区域面板
        JPanel resultPanel = new JPanel(new GridLayout(2, 1, 10, 10)); // 创建结果区域面板，使用 GridLayout 布局管理器，垂直分为两行，行间距和列间距均为 10 像素
        resultPanel.setBorder(BorderFactory.createTitledBorder("结果区域")); // 设置面板边框标题为“结果区域”

        // eBay 结果区域
        ebayScrollPane.setBorder(BorderFactory.createTitledBorder("eBay 结果"));
        resultPanel.add(ebayScrollPane);// 将 eBay 结果滚动面板添加到结果区域

        // GraphQL 结果区域
        graphqlScrollPane.setBorder(BorderFactory.createTitledBorder("GraphQL 结果"));
        resultPanel.add(graphqlScrollPane);// 将 GraphQL 结果滚动面板添加到结果区域

        // 日志区域面板
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("日志输出"));// 设置面板边框标题为“日志输出”
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);// 将日志文本区域放置在滚动面板中，并添加到日志面板中央

        // 设置不同面板的背景颜色
        inputPanel.setBackground(new Color(240, 240, 240, 55)); // 输入面板的背景颜色，带透明度
        resultPanel.setBackground(new Color(245, 245, 245)); // 结果区域面板的背景颜色
        logPanel.setBackground(new Color(255, 255, 255)); // 日志区域面板的背景颜色
        frame.getContentPane().setBackground(new Color(172, 74, 74, 55)); // 主窗口内容背景颜色，带透明度

        // 将不同的面板添加到主窗口中
        frame.add(resultPanel, BorderLayout.CENTER); // 将结果面板添加到窗口中部
        frame.add(logPanel, BorderLayout.SOUTH); // 将日志面板添加到窗口底部
        frame.add(inputPanel, BorderLayout.NORTH); // 将输入面板添加到窗口顶部
        frame.setVisible(true); // 设置窗口可见


        // 获取 eBay 商品信息 为 fetchButton 按钮添加一个点击事件监听器
        fetchButton.addActionListener(e -> {
            // 从输入框中获取用户输入的物品 ID，并去掉首尾的空格
            String itemId = inputField.getText().trim();
            // 如果物品 ID 为空，记录错误日志并退出事件处理
            if (itemId.isEmpty()) {
                logAction(logArea, "[eBay] 错误: 物品ID不能为空！"); // 记录日志到日志区域
                return;// 提前结束处理
            }

            // 设置 eBay 结果区域显示“加载中...”提示
            ebayResultArea.setText("加载中...");
            logAction(logArea, "[eBay] 开始获取项目ID: " + itemId);// 记录开始获取数据的日志
            // 使用新线程处理网络请求，避免阻塞主线程（UI 线程）
            new Thread(() -> {
                try {
                    // 调用方法发送 eBay 请求，并获取响应（XML 格式）
                    String response = sendEbayRequest(itemId);
                    // 将 XML 格式的响应转换为 JSON 格式
                    String jsonResponse = convertXmlToJson(response);
                    // 从响应中提取 SKU 列表
                    List<String> skus = extractSKUs(response);

                    // 使用 Swing 的工具类在事件调度线程上更新 UI（线程安全）
                    SwingUtilities.invokeLater(() -> {
                        ebayResultArea.setText(jsonResponse);// 在 eBay 结果区域显示 JSON 响应
                        skuComboBox.removeAllItems();// 清空 SKU 下拉框的内容
                        skuComboBox.addItem("选择子 SKU");// 添加默认提示选项
                        skus.forEach(skuComboBox::addItem); // 将提取的 SKU 列表逐项添加到下拉框
                    });

                    // 记录成功获取数据的日志
                    logAction(logArea, "[eBay] 成功获取项目ID: " + itemId);
                } catch (Exception ex) {
                    // 如果发生异常，在事件调度线程上更新结果区域显示错误信息
                    SwingUtilities.invokeLater(() -> ebayResultArea.setText("错误: " + ex.getMessage()));
                    // 记录错误日志，包括异常的具体信息
                    logAction(logArea, "[eBay] 错误: 无法获取数据 - " + ex.getMessage());
                }
            }).start();// 启动线程
        });



        // 获取 GraphQL 数据// 为 fetchGraphQLButton 按钮添加一个点击事件监听器
        fetchGraphQLButton.addActionListener(e -> {
            // 设置 GraphQL 结果区域显示“正在加载”提示
            graphqlResultArea.setText("正在加载 GraphQL 数据...");
            // 记录开始获取 GraphQL 数据的日志
            logAction(logArea, "[GraphQL] 开始获取数据...");

            // 使用新线程处理网络请求，避免阻塞主线程（UI 线程）
            new Thread(() -> {
                try {
                    // 获取 GraphQL 查询条件，去掉首尾空格
                    String query = graphqlQueryArea.getText().trim();
                    // 如果查询条件为空，记录错误日志并退出线程
                    if (query.isEmpty()) {
                        logAction(logArea, "[GraphQL] 错误: 查询条件不能为空！");
                        return;// 提前结束处理
                    }

                    // 调用 sendGraphQLRequest 方法发送 GraphQL 请求，并获取响应
                    // 调用 sendGraphQLRequest，它内部会调用 decodeIdsInResponse 解码 ID
                    String response = sendGraphQLRequest(query);
                    // 格式化 JSON 响应内容，使其更易读 并更新到界面
                    String formattedResponse = formatJson(response);

                    // 使用 Swing 的工具类在事件调度线程上更新 UI（线程安全）
                    SwingUtilities.invokeLater(() -> graphqlResultArea.setText(formattedResponse));
                    // 记录成功获取数据的日志
                    logAction(logArea, "[GraphQL] 成功获取数据");
                } catch (Exception ex) {
                    // 如果发生异常，在事件调度线程上更新结果区域显示错误信息
                    SwingUtilities.invokeLater(() -> graphqlResultArea.setText("错误: " + ex.getMessage()));
                    // 记录错误日志，包括异常的具体信息
                    logAction(logArea, "[GraphQL] 错误: 数据获取失败 - " + ex.getMessage());
                }
            }).start();// 启动线程
        });

        // 为 skuComboBox 添加一个选择事件监听器
        skuComboBox.addActionListener(e -> {
            // 获取用户选择的 SKU
            String selectedSku = (String) skuComboBox.getSelectedItem();
            // 如果用户未选择有效的 SKU，则记录提示日志并退出处理
            if (selectedSku == null || selectedSku.equals("选择子 SKU")) {
                logAction(logArea, "[GraphQL] 提示: 请选择有效的 SKU！");
                return;// 提前结束处理
            }

            // 提取 SKU 核心部分和后缀部分
            String[] skuParts = selectedSku.split("-"); // 假设 SKU 使用 "-" 分隔
            String coreSku = skuParts[0] + "-" + skuParts[1]; // 核心 SKU（如 "DM7866-202"）
            String skuSuffix = selectedSku.substring(coreSku.length() + 1); // 提取后缀部分 (如 "40-黑棕")

            // 显示加载提示并记录日志
            graphqlResultArea.setText("正在加载 GraphQL 数据...");
            logAction(logArea, "[GraphQL] 开始查询 SKU: " + coreSku + "，匹配后缀: " + skuSuffix);

            // 使用新线程处理 GraphQL 查询，避免阻塞主线程
            new Thread(() -> {
                try {
                    // 第一步：通过核心 SKU 查询对应的 ProductID
                    String query1 = "query MyQuery {\n" +
                            "  ebay_products_connection(where: {title: {_eq: \"" + coreSku + "\"}}) {\n" +
                            "    edges {\n" +
                            "      node {\n" +
                            "        id\n" +
                            "        title\n" +
                            "      }\n" +
                            "    }\n" +
                            "  }\n" +
                            "}";

                    // 发送 GraphQL 请求，并获取响应（响应中应包含 ProductID）
                    String response1 = sendGraphQLRequest(query1); // GraphQL 查询
                    String productId = extractIdFromResponse(response1); // 提取 ProductID
                    // 如果未找到对应的 ProductID，显示错误提示并退出
                    if (productId == null || productId.isEmpty()) {
                        SwingUtilities.invokeLater(() -> graphqlResultArea.setText("错误: 未找到对应的 ProductID"));
                        logAction(logArea, "[GraphQL] 错误: 未找到对应的 ProductID");
                        return;// 提前结束处理
                    }
                    // 记录日志，说明查询成功并获取到了 ProductID
                    logAction(logArea, "[GraphQL] 查询到的 ProductID: " + productId);

                    // 第二步：通过 ProductID 查询变体和价格信息
                    String query2 = "query MyQuery {\n" +
                            "  ebay_product_variations_connection(order_by: {scrape_time: desc},where: {product_id: {_eq: " + productId + "}}) {\n" +
                            "    edges {\n" +
                            "      node {\n" +
                            "        product_id\n" +
                            "        price\n" +
                            "        variation\n" +
                            "        scrape_time\n" +
                            "      }\n" +
                            "    }\n" +
                            "  }\n" +
                            "}";

                    // 发送 GraphQL 请求，并获取响应（响应中应包含变体和价格信息）
                    String response2 = sendGraphQLRequest(query2); // GraphQL 查询
                    // 根据 SKU 的后缀部分过滤变体数据
                    String filteredResponse = filterVariationsBySuffix(response2, skuSuffix); // 按后缀过滤变体

                    // 在主线程中更新结果区域的内容
                    SwingUtilities.invokeLater(() -> graphqlResultArea.setText(filteredResponse));
                    // 记录成功匹配的日志
                    logAction(logArea, "[GraphQL] 成功匹配 SKU 和 Variation 数据");
                } catch (Exception ex) {
                    // 如果发生异常，在主线程中显示错误信息并记录错误日志
                    SwingUtilities.invokeLater(() -> graphqlResultArea.setText("错误: " + ex.getMessage()));
                    logAction(logArea, "[GraphQL] 错误: 查询失败 - " + ex.getMessage());
                }
            }).start(); // 启动线程
        });

        updatePricesButton.addActionListener(e -> {
            try {
                // 获取 GraphQL 结果区域中的文本
                String resultText = graphqlResultArea.getText();
                if (resultText.isEmpty()) {
                    logAction(logArea, "[价格修改] 错误: 没有价格数据可供修改。");
                    return; // 如果没有数据，退出方法
                }

                String selectedSku = (String) skuComboBox.getSelectedItem();
                if (selectedSku == null || selectedSku.equals("选择 SKU")) {
                    logAction(logArea, "[价格修改] 错误: 未选择有效的 SKU。");
                    return;
                }

                // 解析 GraphQL 结果并修改价格
                String[] lines = resultText.split("\n"); // 按行拆分结果
                StringBuilder updatedResults = new StringBuilder(); // 用于存储修改后的结果
                boolean isMatched = false;

                for (String line : lines) {
                    logAction(logArea, "[调试] 当前行: " + line);

                    if (line.contains("Variation:")) {
                        int variationStartIndex = line.indexOf("Variation:") + 10;
                        String variationValue = line.substring(variationStartIndex, line.indexOf(",", variationStartIndex)).trim(); // 提取 Variation 值

                        String[] skuParts = selectedSku.split("-"); // 提取 SKU 后缀
                        String skuSuffix = skuParts[skuParts.length - 2] + "-" + skuParts[skuParts.length - 1];

                        logAction(logArea, "[调试] Variation: " + variationValue + ", SKU 后缀: " + skuSuffix);

                        // 改为宽松匹配：检查 SKU 后缀是否包含 Variation
                        if (skuSuffix.contains(variationValue)) {
                            isMatched = true;

                            int priceStartIndex = line.indexOf("Price:") + 6;
                            int priceEndIndex = line.indexOf(",", priceStartIndex);
                            double originalPrice = Double.parseDouble(line.substring(priceStartIndex, line.indexOf(",", priceStartIndex)).trim());

                            // 获取 scrape_time
                            int scrapeTimeStartIndex = line.indexOf("Scrape Time:") + 13;
                            String scrapeTime = line.substring(scrapeTimeStartIndex).trim();

                            double newPrice = ((originalPrice / priceRule.divisor + priceRule.additionalConstant) / priceRule.multiplier) + 3;
                            newPrice = Math.ceil(newPrice);// 向上取整到整数

                            updatedResults.append("ProductID: ")
                                    .append(line.substring(line.indexOf("ProductID:") + 11, line.indexOf(",", line.indexOf("ProductID:"))))
                                    .append(", Old Price: ").append(originalPrice)
                                    .append(", New Price: ").append(newPrice)
                                    .append(", Variation: ").append(variationValue)
                                    .append(", Scrape Time: ").append(scrapeTime) // 添加 scrape_time
                                    .append(System.lineSeparator());
                            logAction(logArea, "[价格修改] 成功匹配: SKU = " + selectedSku + ", Old Price = " + originalPrice +
                                    ", New Price = " + newPrice + ", Scrape Time = " + scrapeTime);

                        }
                    }
                }


                if (!isMatched) {
                    logAction(logArea, "[价格修改] 提示: 未找到与当前 SKU 匹配的价格记录。");
                    graphqlResultArea.setText("未找到与选择的 SKU 匹配的数据。");
                    return;
                }

                SwingUtilities.invokeLater(() -> graphqlResultArea.setText(updatedResults.toString().trim()));
                logAction(logArea, "[价格修改] 成功: 已修改所有匹配的价格。");
            } catch (Exception ex) {
                logAction(logArea, "[价格修改] 错误: 无法修改价格 - " + ex.getMessage());
            }
        });


        // 为 updatePricesButton 添加点击事件监听器
        uploadToEbayButton.addActionListener(e -> {
            try {
                // 获取输入的 ItemID
                String itemId = itemIdField.getText().trim();
                if (itemId.isEmpty()) {
                    // 如果结果区域没有数据，记录错误日志并退出
                    logAction(logArea, "[上传到 eBay] 错误: ItemID 不能为空！");
                    return;
                }

                // 获取当前选择的 SKU
                String selectedSku = (String) skuComboBox.getSelectedItem();
                if (selectedSku == null || selectedSku.equals("选择 SKU")) {
                    // 如果未选择有效的 SKU，记录错误日志并退出
                    logAction(logArea, "[上传到 eBay] 错误: 未选择有效的 SKU！");
                    return;
                }

                // 提取 SKU 的前缀部分（去掉后缀）
                String[] skuParts = selectedSku.split("-"); // 根据 "-" 分割 SKU
                String coreSku = skuParts[0] + "-" + skuParts[1] + "-" + skuParts[2]; // 拼接前缀部分

                logAction(logArea, "[调试] 使用的 SKU 前缀: " + coreSku);

                // 获取 GraphQL 结果区域的内容
                String resultText = graphqlResultArea.getText();
                if (resultText.isEmpty()) {
                    logAction(logArea, "[上传到 eBay] 错误: 没有找到修改后的价格！");
                    return;
                }

                // 仅提取结果框的第一个数据
                String[] lines = resultText.split("\\n");
                if (lines.length == 0) {
                    logAction(logArea, "[上传到 eBay] 错误: 结果框中没有数据！");
                    return;
                }

                // 提取第一个结果的数据
                String firstLine = lines[0]; // 获取第一个结果
                logAction(logArea, "[调试] 第一个结果行: " + firstLine);

                // 提取 ProductID
                String productId = extractField(firstLine, "ProductID:");
                // 提取 New Price
                String newPriceStr = extractField(firstLine, "New Price:");
                double newPrice = Math.ceil(Double.parseDouble(newPriceStr));//向上取整为整数
                // 提取 SKU
                String variation = extractField(firstLine, "Variation:");

                logAction(logArea, "[上传到 eBay] 上传数据 -> ProductID: " + productId + ", New Price: " + newPrice + ", Variation: " + variation);

                // 构建 XML 请求
                String xmlRequest = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<ReviseFixedPriceItemRequest xmlns=\"urn:ebay:apis:eBLBaseComponents\">\n" +
                        "  <RequesterCredentials>\n" +
                        "    <eBayAuthToken>" + OAUTHFIX_TOKEN + "</eBayAuthToken>\n" +
                        "  </RequesterCredentials>\n" +
                        "  <Item>\n" +
                        "    <ItemID>" + itemId + "</ItemID>\n" +
                        "    <Variations>\n" +
                        "      <Variation>\n" +
                        "        <SKU>" + coreSku + "</SKU>\n" +
                        "        <StartPrice>" + newPrice + "</StartPrice>\n" +
                        "        <Quantity>1</Quantity>" +
                        "      </Variation>\n" +
                        "    </Variations>\n" +
                        "  </Item>\n" +
                        "</ReviseFixedPriceItemRequest>";

                // 使用现有的 HTTP 请求方法发送请求
                String response = sendHttpRequest("ReviseFixedPriceItem", xmlRequest);

                // 显示响应
                logAction(logArea, "[上传到 eBay] 响应: " + response);
                SwingUtilities.invokeLater(() -> graphqlResultArea.setText("eBay 上传成功: \n" + response));
            } catch (Exception ex) {
                logAction(logArea, "[上传到 eBay] 错误: " + ex.getMessage());
            }
        });

    }


    /**
     * 通过 SKU 后缀过滤变体数据
     *
     * @param jsonResponse GraphQL 查询返回的 JSON 响应
     * @param skuSuffix    SKU 的后缀，用于匹配变体
     * @return 返回过滤后的变体信息（包含 ProductID、Price、Variation 和 Scrape Time），或错误提示
     */

    private static String filterVariationsBySuffix(String jsonResponse, String skuSuffix) {
        try {
            // 创建 ObjectMapper 对象，用于解析 JSON 数据
            ObjectMapper objectMapper = new ObjectMapper();
            // 将 JSON 响应解析为树形结构
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            // 获取目标节点：/data/ebay_product_variations_connection/edges
            JsonNode edgesNode = rootNode.at("/data/ebay_product_variations_connection/edges");
            StringBuilder result = new StringBuilder();// 用于存储过滤后的结果

            // 检查 edgesNode 是否是一个数组
            if (edgesNode.isArray()) {
                // 遍历数组中的每个 edge 节点
                for (JsonNode edge : edgesNode) {
                    // 获取每个 edge 节点中的 node 数据
                    JsonNode node = edge.path("node");
                    // 提取 variation 字段的值
                    String variation = node.path("variation").asText();
                    // 检查 variation 是否以指定的 SKU 后缀结尾
                    if (variation.endsWith(skuSuffix)) { // 检查是否与后缀匹配
                        // 提取 ProductID、价格和 Scrape Time
                        String productId = node.path("product_id").asText();// 获取 product_id 字段
                        double price = node.path("price").asDouble();// 获取 price 字段
                        String scrapeTime = node.path("scrape_time").asText(); // 获取 scrape_time

                        // 过滤条件：如果价格为 0，则跳过
                        if (price == 0.0) {
                            continue;
                        }

                        // 格式化为简化形式
                        result.append("ProductID: ").append(productId)
                                .append(", Price: ").append(price)
                                .append(", Variation: ").append(variation)
                                .append(", Scrape Time: ").append(scrapeTime) // 添加 scrape_time
                                .append("\n");
                    }
                }
            }

            // 如果结果非空，返回结果字符串；否则返回提示错误信息
            return result.length() > 0 ? result.toString().trim() : "错误: 未找到匹配的变体";
        } catch (Exception e) {
            // 捕获异常并打印堆栈信息（仅用于调试）
            e.printStackTrace();
            // 返回错误信息
            return "错误: 无法解析 GraphQL 响应";
        }
    }
    /**
     * 提取字符串中指定字段的值。
     *
     * @param line      包含字段的字符串
     * @param fieldName 要提取的字段名称（格式：fieldName: value）
     * @return 提取的字段值（去掉首尾空格）
     */

    // 工具方法：提取字段值
    private static String extractField(String line, String fieldName) {
        // 计算字段值的起始索引位置（字段名称后面）
        int startIndex = line.indexOf(fieldName) + fieldName.length();
        // 查找字段值的结束索引（以下一个逗号为结束点）
        int endIndex = line.indexOf(",", startIndex); // 查找下一个逗号作为结束点
        // 如果没有找到逗号（字段值到行尾），将结束索引设置为字符串的末尾
        if (endIndex == -1) {
            endIndex = line.length(); // 如果没有逗号，取到行尾
        }
        // 提取字段值（从起始索引到结束索引）并去掉首尾的空格
        return line.substring(startIndex, endIndex).trim();
    }


    /**
     * 发送 eBay API 请求以获取指定 ItemID 的信息。
     *
     * @param itemId eBay 物品 ID，用于查询的唯一标识符
     * @return 返回 eBay API 的响应结果（XML 格式）
     * @throws Exception 如果请求失败或发生其他错误
     */

    private static String sendEbayRequest(String itemId) throws Exception {
        // 构造 XML 格式的请求体
        String requestBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" // eBay 的 GetItem 请求根节点
                + "<GetItemRequest xmlns=\"urn:ebay:apis:eBLBaseComponents\">"
                + "    <RequesterCredentials>"// 包含用户授权的凭据部分
                + "        <eBayAuthToken>" + OAUTH_TOKEN + "</eBayAuthToken>"// 用户的 OAuth 授权令牌，用于验证请求
                + "    </RequesterCredentials>"
                + "    <ItemID>" + itemId + "</ItemID>"// 要查询的物品 ID
                + "</GetItemRequest>";
        // 调用 sendHttpRequest 方法发送 HTTP 请求，并返回响应结果
        // 参数 "GetItem" 是 API 操作名，用于标识请求的类型
        return sendHttpRequest("GetItem", requestBody);
    }

    /**
     * 发送 GraphQL 请求并返回响应数据。
     *
     * @param query GraphQL 查询字符串
     * @return 解码后的 GraphQL 响应（JSON 格式）
     * @throws Exception 如果请求发送失败或发生其他错误
     */
    private static String sendGraphQLRequest(String query) throws Exception {
        // 创建一个 HttpClient 实例，用于发送 HTTP 请求
        HttpClient client = HttpClient.newHttpClient();
        // 创建 ObjectMapper 对象，用于将 Java 对象转换为 JSON 格式
        ObjectMapper objectMapper = new ObjectMapper();

        // 使用 QueryPayload 类封装 GraphQL 查询，并将其序列化为 JSON 格式字符串
        // QueryPayload 是一个自定义的类，通常包含一个字段（如 query）用于封装 GraphQL 查询
        String requestBody = objectMapper.writeValueAsString(new QueryPayload(query));

        // 构建 HTTP 请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(GRAPHQL_URL))// 设置 GraphQL 请求的目标 URI
                .header("Content-Type", "application/json") // 设置请求内容类型为 JSON
                .header("x-hasura-admin-secret", GRAPHQL_ADMIN_SECRET)//设置 GraphQL 的认证密钥
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))// 设置 POST 方法和请求体
                .build();

        // 发送 HTTP 请求，并获取响应
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // 调用解码函数，将响应的 JSON 数据进行解码，并返回解码后的结果
        return decodeIdsInResponse(response.body());
//        return response.body();
    }


    private static String decodeIdsInResponse(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);

            // 遍历 "edges" 节点
            JsonNode edges = root.at("/data/ebay_products_connection/edges");
            if (edges.isArray()) {
                for (JsonNode edge : edges) {
                    JsonNode node = edge.path("node");
                    if (node.has("id")) {
                        String encodedId = node.path("id").asText();
                        String decodedId = decodeBase64(encodedId); // 调用解码方法
                        ((com.fasterxml.jackson.databind.node.ObjectNode) node).put("id", decodedId); // 替换为解码后的 ID
                    }
                }
            }

            // 将修改后的 JSON 转为字符串并返回
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            e.printStackTrace();
            return response; // 如果解码失败，返回原始 JSON 响应
        }
    }


    /**
     * 解码 GraphQL 响应中的 Base64 编码 ID。
     *
     * @param response 原始 GraphQL 响应（JSON 格式）
     * @return 返回替换了解码后 ID 的 JSON 字符串，如果解码失败则返回原始响应
     */
    private static String extractIdFromResponse(String response) {
        try {
            // 创建 ObjectMapper 实例，用于解析和操作 JSON 数据
            ObjectMapper objectMapper = new ObjectMapper();
            // 将 JSON 响应字符串解析为 JsonNode 树形结构
            JsonNode rootNode = objectMapper.readTree(response);
            // 定位到 /data/ebay_products_connection/edges 节点
            JsonNode edgesNode = rootNode.at("/data/ebay_products_connection/edges");
            // 检查 edges 节点是否为数组
            if (edgesNode.isArray() && edgesNode.size() > 0) {
                JsonNode firstNode = edgesNode.get(0).path("node");
                String idValue = firstNode.path("id").asText();

                // 如果 ID 是类似 "[1, \"public\", \"ebay_products\", 21089]" 的复杂格式，则解析数字部分
                if (idValue.startsWith("[") && idValue.endsWith("]")) {
                    // 提取最后一个数字部分作为 ID
                    String[] parts = idValue.split(",");
                    return parts[parts.length - 1].replaceAll("[\\[\\]\"]", "").trim();
                }
                return idValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // 如果未找到 ID，返回 null
    }


    // 通用的 HTTP 请求方法
    /**
     * 发送 HTTP 请求到 eBay API，并返回响应内容。
     *
     * @param apiCallName  API 调用名称，用于指定 API 操作（例如 "GetItem"）
     * @param requestBody  请求的 XML 格式内容
     * @return 返回 eBay API 的响应内容（字符串形式）
     * @throws Exception 如果请求失败或发生其他错误
     */
    private static String sendHttpRequest(String apiCallName, String requestBody) throws Exception {
        // 创建 HttpClient 实例，用于发送 HTTP 请求
        HttpClient client = HttpClient.newHttpClient();

        // 构建 HTTP 请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL))// 设置目标 API 的 URL
                .header("X-EBAY-API-COMPATIBILITY-LEVEL", "967")// 设置 eBay API 的兼容性级别
                .header("X-EBAY-API-SITEID", "0")// 设置 eBay API 的站点 ID
                .header("X-EBAY-API-CALL-NAME", apiCallName)// 设置 API 调用名称
                .header("X-EBAY-API-APP-NAME", "-v11i1f0p-PRD-bb1843f11-acc468fd")// 设置 eBay 应用名称
                .header("X-EBAY-API-DEV-NAME", "992b8f9c-f623-4d1f-8e5d-7095ad651219")// 设置开发者名称
                .header("X-EBAY-API-CERT-NAME", "PRD-941052cfee4f-dee4-4f23-8676-023d")// 设置认证名称
                .header("Content-Type", "text/xml;charset=utf-8") // 设置请求的内容类型为 XML
                .POST(HttpRequest.BodyPublishers.ofString(requestBody)) // 设置 POST 方法并附加请求体
                .build();

        // 发送请求并获取响应
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // 返回响应体内容
        return response.body();
    }

    /**
     * 从 XML 响应中提取所有的 SKU 列表。
     *
     * @param xmlResponse eBay API 返回的 XML 响应字符串
     * @return 包含所有提取到的 SKU 的列表
     * @throws Exception 如果解析 XML 失败
     */
    private static List<String> extractSKUs(String xmlResponse) throws Exception {
        // 创建一个空的列表，用于存储提取到的 SKU
        List<String> skus = new ArrayList<>();
        // 创建 XmlMapper 实例，用于解析 XML 数据
        XmlMapper xmlMapper = new XmlMapper();
        // 将 XML 数据解析为 JsonNode 树形结构
        JsonNode node = xmlMapper.readTree(new StringReader(xmlResponse));

        // 定位到 Variations 节点
        JsonNode variations = node.at("/Item/Variations/Variation");
        // 检查 Variations 是否是数组节点
        if (variations.isArray()) {
            // 遍历数组中的每个 Variation 节点
            for (JsonNode variation : variations) {
                // 提取 SKU 值
                String sku = variation.at("/SKU").asText(null);
                if (sku != null && !sku.isEmpty()) {
                    skus.add(sku);// 如果 SKU 不为空，添加到列表中
                }
            }
        }
        // 返回提取到的 SKU 列表
        return skus;
    }

    /**
     * 将 XML 数据转换为 JSON 格式。
     *
     * @param xmlData 输入的 XML 数据字符串
     * @return 转换后的 JSON 字符串，如果转换失败则返回错误信息
     */
    private static String convertXmlToJson(String xmlData) {
        try {
            // 使用 XmlMapper 解析 XML 数据
            XmlMapper xmlMapper = new XmlMapper();
            // 使用 ObjectMapper 构造 JSON
            ObjectMapper objectMapper = new ObjectMapper();
            // 将 XML 数据解析为 JsonNode 对象
            JsonNode node = xmlMapper.readTree(new StringReader(xmlData));
            // 使用 ObjectMapper 将 JsonNode 转换为格式化后的 JSON 字符串
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            // 如果转换失败，返回包含错误信息的 JSON 字符串
            return "{\"error\": \"XML 转换失败: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 解码 Base64 编码的字符串。
     *
     * @param encodedId 输入的 Base64 编码字符串
     * @return 解码后的字符串，如果输入不是合法的 Base64 则返回原始字符串
     */
    private static String decodeBase64(String encodedId) {
        try {
            // 使用 Java 内置的 Base64 解码器解码字符串
            return new String(java.util.Base64.getDecoder().decode(encodedId));
        } catch (IllegalArgumentException e) {
            // 如果输入不是合法的 Base64 编码，直接返回原始字符串
            return encodedId; // 如果不是 Base64 编码，直接返回原值
        }
    }


    /**
     * 格式化 JSON 字符串，使其具有更易读的缩进结构。
     *
     * @param jsonString 输入的 JSON 字符串
     * @return 格式化后的 JSON 字符串，如果格式化失败则返回原始字符串
     */
    private static String formatJson(String jsonString) {
        try {
            // 使用 ObjectMapper 将 JSON 字符串解析为 Java 对象
            ObjectMapper objectMapper = new ObjectMapper();
            Object json = objectMapper.readValue(jsonString, Object.class);
            // 使用 ObjectMapper 将 Java 对象重新序列化为格式化后的 JSON 字符串
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            // 如果格式化失败，返回原始的 JSON 字符串
            return jsonString; // 返回原始 JSON 字符串
        }
    }


    /**
     * 在日志区域 (JTextArea) 和全局日志记录器中记录一条日志信息。
     *
     * @param logArea  用于显示日志消息的 JTextArea 组件
     * @param message  要记录的日志消息
     */
    private static void logAction(JTextArea logArea, String message) {
        // 拼接日志消息，前缀添加当前时间戳
        String logMessage = "[" + java.time.LocalTime.now() + "] " + message;
        // 使用 SwingUtilities.invokeLater 确保日志更新在事件调度线程中执行（线程安全）
        SwingUtilities.invokeLater(() -> logArea.append(logMessage + "\n"));
        // 使用全局日志记录器记录日志消息
        logger.info(logMessage);
    }

    /**
     * 设置日志记录器，将日志输出到文件并配置格式。
     *
     * 日志记录器主要用于记录应用程序的运行信息、错误信息等，以便于调试和排查问题。
     */
    private static void setupLogger() {
        try {
            // 创建一个 FileHandler，用于将日志输出到 "ebay_api.log" 文件中
            // 第二个参数为 true，表示以追加模式写入日志文件
            FileHandler fileHandler = new FileHandler("ebay_api.log", true);
            // 设置日志文件的格式为简单格式（每条日志记录一行）
            fileHandler.setFormatter(new SimpleFormatter());
            // 将 FileHandler 添加到全局日志记录器中
            logger.addHandler(fileHandler);
            // 设置日志记录器的日志级别为 INFO
            // 表示只记录 INFO 级别及以上（如 WARNING、SEVERE）的日志
            logger.setLevel(Level.INFO);
        } catch (Exception e) {
            // 如果设置日志记录器失败，打印异常堆栈信息
            e.printStackTrace();
        }
    }

    /**
     * 封装类，用于构造 GraphQL 请求的 JSON 数据。
     *
     * GraphQL 请求通常需要一个 "query" 字段，包含查询字符串。
     */
    private static class QueryPayload {
        // 公共字段，用于存储 GraphQL 查询字符串
        public String query;
        /**
         * 构造函数，接收一个 GraphQL 查询字符串并初始化。
         *
         * @param query GraphQL 查询字符串
         */
        public QueryPayload(String query) {
            this.query = query;// 初始化查询字段
        }
    }
}
