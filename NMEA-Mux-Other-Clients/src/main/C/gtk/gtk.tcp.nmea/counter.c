#include <gtk/gtk.h>

// An example...

int count = 0;

void end_program (GtkWidget *wid, gpointer ptr) {
    gtk_main_quit();
}

void count_button (GtkWidget *wid, gpointer ptr) {
    char buffer[80];
    count++;
    sprintf(buffer, "Button pressed %d times", count);
    gtk_label_set_text(GTK_LABEL(ptr), buffer); // Update the label
}

int main (int argc, char *argv[]) {
    gtk_init(&argc, &argv);
    GtkWidget *win = gtk_window_new(GTK_WINDOW_TOPLEVEL);
    GtkWidget *btn = gtk_button_new_with_label("Close window");
    g_signal_connect(btn, "clicked", G_CALLBACK(end_program), NULL);
    g_signal_connect(win, "delete_event", G_CALLBACK(end_program), NULL); // The close button in the header
    GtkWidget *lbl = gtk_label_new("Counter...                                                                       ");
    //                              |                                                                               |
    //                              |                                                                               79
    //                              0
    GtkWidget *btn2 = gtk_button_new_with_label("Count button");
    g_signal_connect(btn2, "clicked", G_CALLBACK(count_button), lbl);
    GtkWidget *box = gtk_vbox_new(FALSE, 5);
    gtk_box_pack_start(GTK_BOX(box), btn2, TRUE, TRUE, 0);
    gtk_box_pack_start(GTK_BOX(box), lbl, TRUE, TRUE, 0);
    gtk_box_pack_start(GTK_BOX(box), btn, TRUE, TRUE, 0);
    gtk_container_add(GTK_CONTAINER(win), box);
    gtk_widget_show_all(win);
    gtk_main();
    return 0;
}