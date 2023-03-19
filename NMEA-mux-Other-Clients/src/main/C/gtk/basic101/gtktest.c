#include <gtk/gtk.h>

void end_program (GtkWidget *wid, gpointer ptr) {
    gtk_main_quit();
}

int main (int argc, char *argv[]) {
    gtk_init(&argc, &argv);
    GtkWidget *win = gtk_window_new(GTK_WINDOW_TOPLEVEL);
    g_signal_connect(win, "delete_event", G_CALLBACK(end_program), NULL); // The close button in the header
    gtk_widget_show(win);
    gtk_main ();
    return 0;
}
