package org.realityforge.gwt.websockets.example.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/chat")
public class ChatServer
{
  private static final Logger LOG = Logger.getLogger( ChatServer.class.getName() );

  private static Set<Session> peers = Collections.synchronizedSet( new HashSet<Session>() );

  @OnOpen
  public void onOpen( final Session session )
  {
    LOG.info( "onOpen(" + session.getId() + ")" );
    peers.add( session );
  }

  @OnClose
  public void onClose( final Session session )
  {
    LOG.info( "onClose(" + session.getId() + ")" );
    peers.remove( session );
  }

  @OnMessage
  public void onMessage( final String message, final Session session )
  {
    LOG.info( "onMessage(" + message + "," + session.getId() + ")" );
    final String id = session.getId();
    for ( final Session peer : peers )
    {
      if ( peer.getId().equals( session.getId() ) )
      {
        peer.getAsyncRemote().sendText( "You said " + message );
      }
      else
      {
        peer.getAsyncRemote().sendText( id + " says " + message );
      }
    }
  }
}
