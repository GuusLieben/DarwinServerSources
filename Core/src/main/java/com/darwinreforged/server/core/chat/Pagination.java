package com.darwinreforged.server.core.chat;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.player.PlayerManager;
import com.darwinreforged.server.core.types.living.MessageReceiver;

import java.util.List;

public class Pagination {
    private Text padding;
    private int linesPerPage = -1;

    private Text header;
    private Text footer;

    private Text title;
    private List<Text> contents;

    public Pagination(Text padding, int linesPerPage, Text header, Text footer, Text title, List<Text> contents) {
        this.padding = padding;
        this.linesPerPage = linesPerPage;
        this.header = header;
        this.footer = footer;
        this.title = title;
        this.contents = contents;
    }

    public static PaginationBuilder builder() {
        return new PaginationBuilder();
    }

    public void sendTo(MessageReceiver receiver) {
        DarwinServer.getUtilMan().get(PlayerManager.class).sendPagination(receiver, this);
    }

    public Text getPadding() {
        return padding;
    }

    public void setPadding(Text padding) {
        this.padding = padding;
    }

    public int getLinesPerPage() {
        return linesPerPage;
    }

    public void setLinesPerPage(int linesPerPage) {
        this.linesPerPage = linesPerPage;
    }

    public Text getHeader() {
        return header;
    }

    public void setHeader(Text header) {
        this.header = header;
    }

    public Text getFooter() {
        return footer;
    }

    public void setFooter(Text footer) {
        this.footer = footer;
    }

    public Text getTitle() {
        return title;
    }

    public void setTitle(Text title) {
        this.title = title;
    }

    public List<Text> getContents() {
        return contents;
    }

    public void setContents(List<Text> contents) {
        this.contents = contents;
    }

    public static final class PaginationBuilder {
        private Text padding;
        private int linesPerPage = -1;
        private Text header;
        private Text footer;
        private Text title;
        private List<Text> contents;

        private PaginationBuilder() {
        }

        public PaginationBuilder padding(Text padding) {
            this.padding = padding;
            return this;
        }

        public PaginationBuilder linesPerPage(int linesPerPage) {
            this.linesPerPage = linesPerPage;
            return this;
        }

        public PaginationBuilder header(Text header) {
            this.header = header;
            return this;
        }

        public PaginationBuilder footer(Text footer) {
            this.footer = footer;
            return this;
        }

        public PaginationBuilder title(Text title) {
            this.title = title;
            return this;
        }

        public PaginationBuilder contents(List<Text> contents) {
            this.contents = contents;
            return this;
        }

        public Pagination build() {
            return new Pagination(
                    this.padding,
                    this.linesPerPage,
                    this.header,
                    this.footer,
                    this.title,
                    this.contents
            );
        }
    }
}
