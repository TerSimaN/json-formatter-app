package json.formatter.app.constants;

import java.net.URL;

import javax.swing.ImageIcon;

public class ImageIconConstants {
    public final ImageIcon arrowLeftBoldIcon;
    public final ImageIcon arrowRightBoldIcon;
    public final ImageIcon arrowUpBoldIcon;
    public final ImageIcon arrowDownBoldIcon;
    public final ImageIcon newFileIcon;
    public final ImageIcon openFileIcon;
    public final ImageIcon saveFileIcon;
    public final ImageIcon copyFileIcon;
    public final ImageIcon formatJsonIcon;
    public final ImageIcon compactJsonIcon;
    public final ImageIcon lineWrapEnableIcon;
    public final ImageIcon lineWrapDisableIcon;
    public final ImageIcon findReplaceIcon;

    public ImageIconConstants() {
        arrowLeftBoldIcon = createImageIcon("icons/arrow-left-bold.png");
        arrowRightBoldIcon = createImageIcon("icons/arrow-right-bold.png");
        arrowUpBoldIcon = createImageIcon("icons/arrow-up-bold.png");
        arrowDownBoldIcon = createImageIcon("icons/arrow-down-bold.png");
        newFileIcon = createImageIcon("icons/new-file.png");
        openFileIcon = createImageIcon("icons/open-file.png");
        saveFileIcon = createImageIcon("icons/save-file.png");
        copyFileIcon = createImageIcon("icons/copy-file.png");
        formatJsonIcon = createImageIcon("icons/format-json.png");
        compactJsonIcon = createImageIcon("icons/compact-json.png");
        lineWrapEnableIcon = createImageIcon("icons/line-wrap-enabled.png");
        lineWrapDisableIcon = createImageIcon("icons/line-wrap-disabled.png");
        findReplaceIcon = createImageIcon("icons/text-search-replace.png");
    }

    private ImageIcon createImageIcon(String path) {
        URL imgUrl = getClass().getResource(path);
        if (imgUrl != null) {
            return new ImageIcon(imgUrl);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}
