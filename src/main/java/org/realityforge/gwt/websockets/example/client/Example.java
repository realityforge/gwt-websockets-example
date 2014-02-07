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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.realityforge.gwt.websockets.client.WebSocket;
import org.realityforge.gwt.websockets.client.event.CloseEvent;
import org.realityforge.gwt.websockets.client.event.ErrorEvent;
import org.realityforge.gwt.websockets.client.event.MessageEvent;
import org.realityforge.gwt.websockets.client.event.OpenEvent;

public final class Example
  implements EntryPoint
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
        onOpen( webSocket );
      }
    } );
    webSocket.addCloseHandler( new CloseEvent.Handler()
    {
      @Override
      public void onCloseEvent( @Nonnull final CloseEvent event )
      {
        onClose( webSocket );
      }
    } );
    webSocket.addErrorHandler( new ErrorEvent.Handler()
    {
      @Override
      public void onErrorEvent( @Nonnull final ErrorEvent event )
      {
        onError( webSocket );
      }
    } );
    webSocket.addMessageHandler( new MessageEvent.Handler()
    {
      @Override
      public void onMessageEvent( @Nonnull final MessageEvent event )
      {
        onMessage( event, webSocket );
      }
    } );
  }

  private void onMessage( final MessageEvent event, final WebSocket webSocket )
  {
    logStatus( "Message", webSocket );
    if ( MessageEvent.DataType.TEXT == event.getDataType() )
    {
      appendText( "message: " + event.getTextData(), "black" );
    }
    else
    {
      final ArrayBuffer data = event.getArrayBufferData();
      final Int8Array arrayBuffer = TypedArrays.createInt8Array( data );
      final StringBuilder sb = new StringBuilder();
      for ( int i = 0; i < arrayBuffer.length(); i++ )
      {
        sb.append( (char) arrayBuffer.get( i ) );
      }
      appendText( "binary message: " + sb, "black" );
    }
  }

  private void onError( final WebSocket webSocket )
  {
    logStatus( "Error", webSocket );
    appendText( "error", "red" );
    _connect.setEnabled( false );
    _disconnect.setEnabled( false );
    _send.setEnabled( false );
  }

  private void onClose( final WebSocket webSocket )
  {
    logStatus( "Close", webSocket );
    appendText( "close", "silver" );
    _connect.setEnabled( true );
    _disconnect.setEnabled( false );
    _send.setEnabled( false );
  }

  private void onOpen( final WebSocket webSocket )
  {
    logStatus( "Open", webSocket );
    appendText( "open", "silver" );
    _disconnect.setEnabled( true );
    _send.setEnabled( true );
  }

  private void logStatus( @Nonnull final String section,
                          @Nonnull final WebSocket webSocket )
  {
    LOG.warning( "WebSocket @ " + section + "\n" +
                 "URL:" + webSocket.getURL() + "\n" +
                 "BinaryType:" + webSocket.getBinaryType() + "\n" +
                 "BufferedAmount:" + webSocket.getBufferedAmount() + "\n" +
                 "Extensions:" + webSocket.getExtensions() + "\n" +
                 "Protocol:" + webSocket.getProtocol() + "\n" +
                 "ReadyState:" + webSocket.getReadyState() );
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
