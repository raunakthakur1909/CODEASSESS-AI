export interface SseClientOptions {
  onEvent: (name: string, data: string) => void;
  onError: (error: Error) => void;
  signal: AbortSignal;
}

export async function connectSse(
  url: string,
  token: string,
  options: SseClientOptions
): Promise<void> {
  const { onEvent, onError, signal } = options;

  try {
    const response = await fetch(url, {
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: 'text/event-stream',
      },
      signal,
    });

    if (!response.ok) {
      throw new Error(`SSE connection failed: ${response.status} ${response.statusText}`);
    }

    if (!response.body) {
      throw new Error('SSE connection failed: readable stream not supported');
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      
      let newlineIndex;
      // SSE frames are separated by \n\n (or \r\n\r\n but typically \n\n)
      // Some servers might mix \r\n, so we normalize the buffer or just look for \n\n.
      // Normalize \r\n to \n to make parsing easier:
      buffer = buffer.replace(/\r\n/g, '\n');

      while ((newlineIndex = buffer.indexOf('\n\n')) >= 0) {
        const frame = buffer.slice(0, newlineIndex);
        buffer = buffer.slice(newlineIndex + 2);

        parseFrame(frame, onEvent);
      }
    }
  } catch (err: any) {
    if (err.name === 'AbortError') {
      return; // Expected disconnection
    }
    onError(err);
  }
}

function parseFrame(frame: string, onEvent: (name: string, data: string) => void) {
  const lines = frame.split('\n');
  let eventName = 'message';
  let data = '';

  for (const line of lines) {
    if (line.startsWith(':')) {
      // Comment / heartbeat - ignore
      continue;
    }

    const colonIndex = line.indexOf(':');
    if (colonIndex === -1) {
      continue;
    }

    const field = line.slice(0, colonIndex);
    const value = line.slice(colonIndex + 1).replace(/^ /, '');

    if (field === 'event') {
      eventName = value;
    } else if (field === 'data') {
      data += data ? '\n' + value : value;
    }
  }

  if (data) {
    onEvent(eventName, data);
  }
}
