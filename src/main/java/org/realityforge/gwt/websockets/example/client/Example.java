package org.realityforge.gwt.websockets.example.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.realityforge.gwt.websockets.client.WebSocket;
import org.realityforge.gwt.websockets.client.WebSocketListener;

public final class Example
  implements EntryPoint, WebSocketListener
{
  private static final Logger LOG = Logger.getLogger( Example.class.getName() );

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
      webSocket.setListener( this );
      final TextBox input = new TextBox();
      input.setValue( "Greetings!" );

      final CheckBox checkBox = new CheckBox( "Binary?" );

      _connect = new Button( "Connect", new ClickHandler()
      {
        @Override
        public void onClick( final ClickEvent event )
        {
          _connect.setEnabled( false );
          webSocket.connect( getWebSocketURL() );
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
          send( webSocket, input.getValue(), Boolean.TRUE == checkBox.getValue() );
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
        controls.add( _connect );
        controls.add( _disconnect );
        RootPanel.get().add( controls );
      }

      {
        final FlowPanel controls = new FlowPanel();
        controls.add( input );
        controls.add( checkBox );
        controls.add( _send );
        RootPanel.get().add( controls );
      }
    }
  }

  private String getWebSocketURL()
  {
    final String moduleBaseURL = GWT.getHostPageBaseURL();
    return moduleBaseURL.replaceFirst( "^http\\:", "ws:" ) + "chat";
  }

  private void send( final WebSocket webSocket, final String message, final boolean binary )
  {
    if ( binary )
    {
      final Int8Array arrayBuffer = TypedArrays.createInt8Array( message.length() );
      arrayBuffer.set( message.getBytes() );
      webSocket.send( arrayBuffer );
    }
    else
    {
      webSocket.send( message );
    }
  }

  public void onMessage( @Nonnull final WebSocket webSocket, @Nonnull final ArrayBuffer data )
  {
    logStatus( "Message", webSocket );
    final Int8Array arrayBuffer = TypedArrays.createInt8Array( data );
    final StringBuilder sb = new StringBuilder();
    for ( int i = 0; i < arrayBuffer.length(); i++ )
    {
      sb.append( (char) arrayBuffer.get( i ) );
    }
    appendText( "binary message: " + sb, "black" );
  }

  public void onMessage( @Nonnull final WebSocket webSocket, @Nonnull final String textData )
  {
    logStatus( "Message", webSocket );
    appendText( "message: " + textData, "black" );
  }

  public void onError( @Nonnull final WebSocket webSocket )
  {
    logStatus( "Error", webSocket );
    appendText( "error", "red" );
    _connect.setEnabled( false );
    _disconnect.setEnabled( false );
    _send.setEnabled( false );
  }

  @Override
  public void onClose( @Nonnull final WebSocket webSocket,
                       final boolean wasClean,
                       final int code,
                       @Nullable final String reason )
  {
    logStatus( "Close", webSocket );
    appendText( "close", "silver" );
    _connect.setEnabled( true );
    _disconnect.setEnabled( false );
    _send.setEnabled( false );
  }

  public void onOpen( @Nonnull final WebSocket webSocket )
  {
    logStatus( "Open", webSocket );
    appendText( "open", "silver" );
    _disconnect.setEnabled( true );
    _send.setEnabled( true );
  }

  private void logStatus( @Nonnull final String section,
                          @Nonnull final WebSocket webSocket )
  {
    final String suffix = !webSocket.isConnected() ?
                          "" :
                          "URL:" + webSocket.getURL() + "\n" +
                          "BinaryType:" + webSocket.getBinaryType() + "\n" +
                          "BufferedAmount:" + webSocket.getBufferedAmount() + "\n" +
                          "Extensions:" + webSocket.getExtensions() + "\n" +
                          "Protocol:" + webSocket.getProtocol();
    LOG.warning( "WebSocket @ " + section + "\n" + "ReadyState:" + webSocket.getReadyState() + "\n" + suffix );
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
