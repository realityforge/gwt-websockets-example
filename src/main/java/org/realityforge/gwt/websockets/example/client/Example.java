package org.realityforge.gwt.websockets.example.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import javax.annotation.Nonnull;
import org.realityforge.gwt.websockets.client.WebSocket;
import org.realityforge.gwt.websockets.client.event.CloseEvent;
import org.realityforge.gwt.websockets.client.event.ErrorEvent;
import org.realityforge.gwt.websockets.client.event.MessageEvent;
import org.realityforge.gwt.websockets.client.event.OpenEvent;

public final class Example
  implements EntryPoint
{
  private HTML _messages;
  private ScrollPanel _scrollPanel;
  private Button _disconnect;
  private Button _connect;
  private Button _send;

  public void onModuleLoad()
  {
    final WebSocket webSocket = WebSocket.newWebSocketIfSupported();
    if ( null == webSocket )
    {
      Window.alert( "WebSocket not available!" );
    }
    else
    {
      registerListeners( webSocket );
      final TextBox url = new TextBox();
      final String moduleBaseURL = GWT.getModuleBaseURL();
      final String moduleName = GWT.getModuleName();
      final String webSocketURL =
        moduleBaseURL.substring( 0, moduleBaseURL.length() - moduleName.length() - 1 ).
          replaceFirst( "^http\\:", "ws:" ) + "chat";
      url.setValue( webSocketURL );
      final TextBox input = new TextBox();
      input.setValue( "Greetings!" );

      _connect = new Button( "Connect", new ClickHandler()
      {
        @Override
        public void onClick( final ClickEvent event )
        {
          _connect.setEnabled( false );
          webSocket.connect( url.getValue() );
        }
      } );
      _disconnect = new Button( "Disconnect", new ClickHandler()
      {
        @Override
        public void onClick( ClickEvent event )
        {
          webSocket.close();
          _disconnect.setEnabled( false );
        }
      } );
      _disconnect.setEnabled( false );
      _send = new Button( "Send", new ClickHandler()
      {
        @Override
        public void onClick( ClickEvent event )
        {
          webSocket.send( input.getValue() );
        }
      } );
      _send.setEnabled( false );

      _messages = new HTML();
      _scrollPanel = new ScrollPanel();
      _scrollPanel.setHeight( "250px" );
      _scrollPanel.add( _messages );
      RootPanel.get().add( _scrollPanel );

      {
        final FlowPanel controls = new FlowPanel();
        controls.add( url );
        controls.add( _connect );
        controls.add( _disconnect );
        RootPanel.get().add( controls );
      }

      {
        final FlowPanel controls = new FlowPanel();
        controls.add( input );
        controls.add( _send );
        RootPanel.get().add( controls );
      }
    }
  }

  private void registerListeners( final WebSocket webSocket )
  {
    webSocket.addOpenHandler( new OpenEvent.Handler()
    {
      @Override
      public void onOpenEvent( @Nonnull final OpenEvent event )
      {
        appendText( "open", "silver" );
        _disconnect.setEnabled( true );
        _send.setEnabled( true );
      }
    } );
    webSocket.addCloseHandler( new CloseEvent.Handler()
    {
      @Override
      public void onCloseEvent( @Nonnull final CloseEvent event )
      {
        appendText( "close", "silver" );
        _connect.setEnabled( true );
        _disconnect.setEnabled( false );
        _send.setEnabled( false );
      }
    } );
    webSocket.addErrorHandler( new ErrorEvent.Handler()
    {
      @Override
      public void onErrorEvent( @Nonnull final ErrorEvent event )
      {
        appendText( "error", "red" );
        _connect.setEnabled( false );
        _disconnect.setEnabled( false );
        _send.setEnabled( false );
      }
    } );
    webSocket.addMessageHandler( new MessageEvent.Handler()
    {
      @Override
      public void onMessageEvent( @Nonnull final MessageEvent event )
      {
        appendText( "message: " + event.getData(), "black" );
      }
    } );
  }

  private void appendText( final String text, final String color )
  {
    final DivElement div = Document.get().createDivElement();
    div.setInnerText( text );
    div.setAttribute( "style", "color:" + color );
    _messages.getElement().appendChild( div );
    _scrollPanel.scrollToBottom();
  }
}