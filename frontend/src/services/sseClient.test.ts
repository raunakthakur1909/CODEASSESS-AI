import { describe, it, expect, vi, beforeEach } from 'vitest';
import { connectSse } from './sseClient';

describe('sseClient', () => {
  let mockFetch: ReturnType<typeof vi.fn>;
  let mockReader: any;

  beforeEach(() => {
    mockReader = {
      read: vi.fn(),
    };

    mockFetch = vi.fn().mockResolvedValue({
      ok: true,
      body: {
        getReader: () => mockReader,
      },
    });

    vi.stubGlobal('fetch', mockFetch);
  });

  const createStreamData = (texts: string[]) => {
    const encoder = new TextEncoder();
    for (let i = 0; i < texts.length; i++) {
      mockReader.read.mockResolvedValueOnce({
        done: false,
        value: encoder.encode(texts[i]),
      });
    }
    mockReader.read.mockResolvedValueOnce({ done: true, value: undefined });
  };

  it('includes Authorization header and NOT ?token', async () => {
    createStreamData([]);
    const controller = new AbortController();

    await connectSse('http://api/events', 'my-jwt', {
      onEvent: vi.fn(),
      onError: vi.fn(),
      signal: controller.signal,
    });

    expect(mockFetch).toHaveBeenCalledWith('http://api/events', {
      headers: {
        Authorization: 'Bearer my-jwt',
        Accept: 'text/event-stream',
      },
      signal: controller.signal,
    });
    // Ensure URL doesn't contain token
    expect(mockFetch.mock.calls[0][0]).not.toContain('token=');
  });

  it('parses a single complete event', async () => {
    createStreamData(['event: test\ndata: {"id":1}\n\n']);
    
    const onEvent = vi.fn();
    await connectSse('http://api/events', 'token', {
      onEvent,
      onError: vi.fn(),
      signal: new AbortController().signal,
    });

    expect(onEvent).toHaveBeenCalledWith('test', '{"id":1}');
  });

  it('parses multiple events in one chunk', async () => {
    createStreamData([
      'event: one\ndata: {"id":1}\n\nevent: two\ndata: {"id":2}\n\n'
    ]);
    
    const onEvent = vi.fn();
    await connectSse('http://api/events', 'token', {
      onEvent,
      onError: vi.fn(),
      signal: new AbortController().signal,
    });

    expect(onEvent).toHaveBeenCalledTimes(2);
    expect(onEvent).toHaveBeenNthCalledWith(1, 'one', '{"id":1}');
    expect(onEvent).toHaveBeenNthCalledWith(2, 'two', '{"id":2}');
  });

  it('handles split network chunks gracefully', async () => {
    createStreamData([
      'event: split\nda',
      'ta: {"hel',
      'lo": "world"}\n\n'
    ]);
    
    const onEvent = vi.fn();
    await connectSse('http://api/events', 'token', {
      onEvent,
      onError: vi.fn(),
      signal: new AbortController().signal,
    });

    expect(onEvent).toHaveBeenCalledTimes(1);
    expect(onEvent).toHaveBeenCalledWith('split', '{"hello": "world"}');
  });

  it('ignores heartbeats / comments', async () => {
    createStreamData([
      ': heartbeat\n\nevent: test\ndata: 123\n\n: heartbeat2\n\n'
    ]);
    
    const onEvent = vi.fn();
    await connectSse('http://api/events', 'token', {
      onEvent,
      onError: vi.fn(),
      signal: new AbortController().signal,
    });

    expect(onEvent).toHaveBeenCalledTimes(1);
    expect(onEvent).toHaveBeenCalledWith('test', '123');
  });

  it('handles multiline data', async () => {
    createStreamData([
      'event: multi\ndata: line1\ndata: line2\n\n'
    ]);
    
    const onEvent = vi.fn();
    await connectSse('http://api/events', 'token', {
      onEvent,
      onError: vi.fn(),
      signal: new AbortController().signal,
    });

    expect(onEvent).toHaveBeenCalledWith('multi', 'line1\nline2');
  });

  it('calls onError when fetch fails', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 403,
      statusText: 'Forbidden'
    });

    const onError = vi.fn();
    await connectSse('http://api/events', 'token', {
      onEvent: vi.fn(),
      onError,
      signal: new AbortController().signal,
    });

    expect(onError).toHaveBeenCalledTimes(1);
    expect(onError.mock.calls[0][0].message).toContain('403 Forbidden');
  });
});
